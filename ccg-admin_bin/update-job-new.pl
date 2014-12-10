#!/usr/bin/perl
#

 use DBD::mysql;
 use DateTime::Format::MySQL;
 use DateTime::Format::HTTP;
 use DateTime::Format::Epoch::Unix;
 use Date::Manip;
 use POSIX qw/mktime/;
 use lib /var/www/gms;
 use gms_db_params;

   $DEBUG=$ENV{DEBUG};
   $emailDir="/home/ccg-admin/mail_archive";
   $emailFil=$ARGV[0];
   $emailFil=~/([0-9]{4,})/;
   $localJobID=$1;
   if (!$localJobID) {
      print STDERR "There doesn't seem to be a localJobID associated with\n";
      print STDERR "$emailFil . Exiting...\n";
      exit;
   }

   $JobFinished=&isJobFinished($localJobID);

sub formatDate {
   my $Date=$_[0];
   my $class = 'DateTime::Format::HTTP';
   $Date = $class->parse_datetime($Date);
   $Date=DateTime::Format::MySQL->format_datetime($Date);
   return $Date;
}

sub secsSinceEpoch {
   my $date=$_[0];
   my $dt=DateTime::Format::HTTP->parse_datetime($date);
   my $sec=DateTime::Format::Epoch::Unix->format_datetime($dt);
   return $sec;
}

sub isJobFinished {
   my $localJobID=$_[0];
   $localJobID =~ /([0-9]{4,})/;
   my $jobID = $1;	#   keep only the numeric part of $localJobID
   $localJobID=$jobID;
   my $finished=0;
#------------------------------------------------------------------------
#         Update all finished jobs' start and end dates, wall time,
#         CPU time and status.
#------------------------------------------------------------------------
   my $startDate, $endDate, $wallTime, $cpuTime, $status;

   my $dbws="GMS2_0";
   my $userID=""; #userid from the gms_param_db 
   my $host=""; # host from the  gms_param_db
   my $passwd=""; # passwd from the  gms_param_db
#  my $passwd=`cat /home/ccg-admin/.p`;
#  chomp $passwd;
   my $connectionInfo="DBI:mysql:database=$dbws;$host";
   if ($DEBUG) {
      print "$connectionInfo,$userID,$passwd\n";
   }
   my $dbh=DBI->connect($connectionInfo,$userID,$passwd);
#   $emailFil="$emailFil";
   if ($DEBUG) {
      print "emailFil = $emailFil\n";
   }

   if ( -e $emailFil ) {
      $fileExists="File exists";
#------------------------------------------------------------------------
#         Find the resource where the job ran:
#------------------------------------------------------------------------
      my $CR;
      if (`egrep "mercury" $emailFil`) {
         $CR='tg-login.ncsa.teragrid.org';
      } elsif (`egrep "ccg-login[a-zA-Z0-9]*.ncsa" $emailFil`) {
	 $CR='ccg-login.ncsa.uiuc.edu';
      } elsif (`egrep "agt\-[a-zA-Z0-9]*.epn" $emailFil`) {
	 $CR='ccg-login.epn.osc.edu';
      } elsif (`egrep "nfs[a-zA-Z0-9]*.osc" $emailFil`) {
	 $CR='tg-login1.osc.edu';
      } elsif (`egrep "longhorn.tacc" $emailFil`) {
	 $CR='longhorn.tacc.utexas.edu';
      } elsif (`egrep "champion.tacc" $emailFil`) {
	 $CR='champion.tacc.utexas.edu';
      } elsif (`egrep "lonestar.tacc" $emailFil`) {
	 $CR='lonestar.tacc.utexas.edu';
      } elsif (`egrep "sdx[0-9a-zA-Z]*.uky" $emailFil`) {
	 $CR='sdx.uky.edu';
      } elsif (`egrep "mike[0-9a-zA-Z]*" $emailFil`) {
         $CR='mike4.cct.lsu.edu';
      } elsif (`egrep "bigred" $emailFil`) {
         $CR='bigred';
      } elsif (`egrep "qb[0-9].loni.org" $emailFil`) {
         $CR='qb1.loni.org';
      } elsif (`egrep "abem[0-9].ncsa.uiuc.edu" $emailFil`) {
         $CR='abe.ncsa.uiuc.edu';
      } elsif (`egrep "trestles" $emailFil`) {
         $CR='trestles.sdsc.teragrid.org';
      } elsif (`egrep "gordon" $emailFil`) {
         $CR='gordon.sdsc.edu';
      } elsif (`egrep "blacklight" $emailFil`) {
         $CR='blacklight.psc.teragrid.org';
      } elsif (`egrep "ember" $emailFil`) {
         $CR='ember.ncsa.illinois.edu';
      } elsif (`egrep "forge" $emailFil`) {
         $CR='forge.ncsa.illinois.edu';
      } elsif (`egrep "ranger.tacc" $emailFil`) {
         $CR='ranger.tacc.teragrid.org';
      } elsif (`egrep "stampede" $emailFil`) {
         $CR='stampede.tacc.utexas.edu';
      } elsif (`egrep "kraken" $emailFil`) {
         $CR='kraken.nics.xsede.org';
      } else{
        die "$0: ERROR - there is no appropriate entry for the comp. resource - maybe you need to add a new entry in $0"; 
      }

      if ($DEBUG) {
          print "CR = $CR\n";
      }

      my $sth = $dbh->prepare("select resourceID from Resources where hostname like '%$CR%';\n") or &dbdie;
      $sth->execute() or &dbdie;
      my $hash_ref=$sth->fetchrow_hashref;
      $resourceID=$hash_ref->{resourceID};
      if ($DEBUG) {
         print "select resourceID from Resources where hostname like '%$CR%';\n";
	 if (!$resourceID) {
	    print "NO RESOURCEID FOUND FOR $CR\n\n";
         } else {
            print "resourceID=$resourceID for CR=$CR\n";
	 }
      }
#------------------------------------------------------------------------
#         Make sure job status is not finished to prevent out-of-order
#         emails from setting a 'finished/runtime_error' status back
#         to 'running.'
#------------------------------------------------------------------------
      my $sthj=$dbh->prepare("select status, jobID from Jobs where localJobID=$localJobID and computeResourceID=$resourceID;");
      $sthj->execute() or &dbdie;
      my $hashrefj=$sthj->fetchrow_hashref;
      $status=$hashrefj->{status};
      if ($status=~/finished/i || $status=~/error/i || $status=~/stopped/i) {
         my $jobID_tmp=$hashrefj->{jobID};
         die "Job with jobID=$jobID_tmp, localJobID=$localJobID and resourceID=$resourceID has already finished\n";
      }
      $sthj->finish();

#------------------------------------------------------------------------
#         Find the scheduler type and whether job has finished
#------------------------------------------------------------------------
      if (`grep "PBS" $emailFil`) {
         $scheduler = "PBS";
	 if (`grep "Execution terminated" $emailFil`) {
	    $finished=1;

	    #   Get Wall Time
	    $cpuTime=`grep "resources_used.cput" $emailFil`;
	    chomp $cpuTime;
	    $cpuTime=~s/resources_used.cput=//;
	    my @cpuTime=split /:/,$cpuTime;
	    $cpuTime=$cpuTime[0]*3600+$cpuTime[1]*60+$cpuTime[2];

	    #   Get Wall Time
	    $wallTime=`grep "resources_used.walltime" $emailFil`;
	    chomp $walltime;
	    $wallTime=~s/resources_used.walltime=//;
	    my @wallTime=split /:/,$wallTime;
	    $wallTime=$wallTime[0]*3600+$wallTime[1]*60+$wallTime[2];

	    #   Get job completion date
	    $endDate=`grep "Date: " $emailFil | tail -n 1`;
	    $endDate=~s/Date: //;
	    chomp $endDate;
	    $endDate=&formatDate($endDate);

	    #   Get job start date
            $dt=DateTime::Format::MySQL->parse_datetime($endDate);
            my $sec1=DateTime::Format::Epoch::Unix->format_datetime($dt);
            my $sec0=$sec1-$wallTime;
            $dt=DateTime->from_epoch(epoch =>$sec0);
            $startDate=DateTime::Format::MySQL->format_datetime($dt);

	    #   Check if job completed successfully
	    $status=`grep "Exit_status=" $emailFil`;
	    chomp $status;
	    $status=~s/Exit_status=//;
	    if ( $status > 0 ) {
	       $status="RUNTIME_ERROR";
	    } else {
	       $status="FINISHED";
	    }

	 } elsif (`grep "Begun execution" $emailFil`) {
	    #   Get job start date
	    $startDate=`grep "Date: " $emailFil | tail -n 1`;
	    $startDate=~s/Date: //;
	    chomp $startDate;
	    $startDate=&formatDate($startDate);
	    #   Set status to "Running"
	    $status="RUNNING";
	    (print "startDate=$startDate\n") if ($DEBUG);
	    $val{startTime}=$startDate;
	    $val{status}=$status;
	 }
      } elsif (`grep "lsf" $emailFil`) {
         $scheduler = "lsf";
	 (print "scheduler=$scheduler\n") if ($DEBUG);

	 if (`grep "Your job looked like" $emailFil`) {
	    #   Get job start date
	    (print "Getting job start date\n") if ($DEBUG);
	    $startDate=`grep "Started at " $emailFil | tail -n 1`;
	    chomp $startDate;
	    (print "startDate=$startDate\n") if ($DEBUG);
	    $startDate=~s/Started at //;
	    (print "startDate=$startDate\n") if ($DEBUG);
	    $startDate=&formatDate($startDate);

	    #   Get job end date
	    $endDate=`grep "Results reported at " $emailFil | tail -n 1`;
	    chomp $endDate;
	    $endDate=~s/Results reported at //;
	    $endDate=&formatDate($endDate);

	    #   Get CPU time
	    $cpuTime=`grep "CPU time   :" $emailFil`;
	    $cpuTime=~s/\s+CPU time\s+\:\s*//;
	    $cpuTime=~s/sec\.//;
	    chomp $cpuTime;

	    #   Get Wall Time
	    my $sec0=secsSinceEpoch($startDate);
	    my $sec1=secsSinceEpoch($endDate);
	    $wallTime=$sec1-$sec0;

            #  Look for job completion status from LSF email
            $exitStatus=`grep "Exited with exit code" $emailFil`;
            chomp $exitStatus;
            $exitStatus=~/Exited with exit code\s*([\d]+)/;
            $exitStatus=$1;
            if ($exitStatus>0) {
               $status="RUNTIME_ERROR";
            } elsif ($exitStatus==0) {
               $status="FINISHED";
            }

	 } else {
	    #   Get job start date
	    $startDate=`grep "Started at " $emailFil | tail -n 1`;
	    chomp $startDate;
	    $startDate=~s/Started at //;
 	    $startDate=&formatDate($startDate);

	    $status="RUNNING";
	    $endDate="NA";
	    $cpuTime="NA";
	    $wallTime="NA";

	 }
      } elsif (`grep "LoadLeveler" $emailFil`) {
         $scheduler = "LoadLeveler";
	 if (`grep "Exited at:" $emailFil`) {
	    #   Get job start date
	    $startDate=`grep "Started at:" $emailFil | tail -n 1`;
	    chomp $startDate;
	    $startDate=~s/^[a-zA-Z0-9\s\.%\<\>]+Started at //;
	    $startDate=~s/Started at://;
	    $startDate=&formatDate($startDate);

	    #   Get job end date
	    $endDate=`grep "Exited at:" $emailFil | tail -n 1`;
	    chomp $endDate;
	    $endDate=~s/Exited at://;
	    $endDate=&formatDate($endDate);

	    #   Get Wall Time
	    my $sec0=secsSinceEpoch($startDate);
	    my $sec1=secsSinceEpoch($endDate);
	    $wallTime=$sec1-$sec0;

	    #   Get CPU time. Not available for LoadL so set to $walltime
	    #   for now
	    $cpuTime=$wallTime;

	    #   Check if job completed successfully
	    my $string=`grep -i "exited normally and returned" $emailFil`;
	    $string=~/exit code of ([0-9]+)/;
	    my $ierr=$1;
	    $status="FINISHED";
	    if ($ierr > 0) {
	       $status="RUNTIME_ERROR";
	    }
	 } 
	 else {
	    $status="RUNNING";
	    #   Get job start date
	    $startDate=`grep "^Date:" $emailFil | head -n1`;
	    chomp $startDate;
	    $startDate=~s/Date: //;
	    $startDate=&formatDate($startDate);

	    $endDate="NA";
	    $wallTime="NA";
	    $cpuTime= "NA";
	 }
      } elsif (`grep "SLURM" $emailFil`) {
         $scheduler = "SLURM";
         if (`grep "Ended" $emailFil`) {
	    $finished=1;

	    #   Get Wall Time
	    $cpuTime=`grep "resources_used.cput" $emailFil`;
	    chomp $cpuTime;
	    $cpuTime=~s/resources_used.cput=//;
	    my @cpuTime=split /:/,$cpuTime;
	    $cpuTime=$cpuTime[0]*3600+$cpuTime[1]*60+$cpuTime[2];

	    #   Get Wall Time
	    $wallTime=`grep "resources_used.walltime" $emailFil`;
	    chomp $walltime;
	    $wallTime=~s/resources_used.walltime=//;
	    my @wallTime=split /:/,$wallTime;
	    $wallTime=$wallTime[0]*3600+$wallTime[1]*60+$wallTime[2];

	    #   Get job completion date
	    $endDate=`grep "Date: " $emailFil | tail -n 1`;
	    $endDate=~s/Date: //;
	    chomp $endDate;
	    $endDate=&formatDate($endDate);

	    #   Get job start date
            $dt=DateTime::Format::MySQL->parse_datetime($endDate);
            my $sec1=DateTime::Format::Epoch::Unix->format_datetime($dt);
            my $sec0=$sec1-$wallTime;
            $dt=DateTime->from_epoch(epoch =>$sec0);
            $startDate=DateTime::Format::MySQL->format_datetime($dt);

	    $status="FINISHED";

	 } elsif (`grep "Failed" $emailFil`) {
            $finished=1;

	    #   Get Wall Time
	    $cpuTime=`grep "resources_used.cput" $emailFil`;
	    chomp $cpuTime;
	    $cpuTime=~s/resources_used.cput=//;
	    my @cpuTime=split /:/,$cpuTime;
	    $cpuTime=$cpuTime[0]*3600+$cpuTime[1]*60+$cpuTime[2];

	    #   Get Wall Time
	    $wallTime=`grep "resources_used.walltime" $emailFil`;
	    chomp $walltime;
	    $wallTime=~s/resources_used.walltime=//;
	    my @wallTime=split /:/,$wallTime;
	    $wallTime=$wallTime[0]*3600+$wallTime[1]*60+$wallTime[2];

	    #   Get job completion date
	    $endDate=`grep "Date: " $emailFil | tail -n 1`;
	    $endDate=~s/Date: //;
	    chomp $endDate;
	    $endDate=&formatDate($endDate);

	    #   Get job start date
            $dt=DateTime::Format::MySQL->parse_datetime($endDate);
            my $sec1=DateTime::Format::Epoch::Unix->format_datetime($dt);
            my $sec0=$sec1-$wallTime;
            $dt=DateTime->from_epoch(epoch =>$sec0);
            $startDate=DateTime::Format::MySQL->format_datetime($dt);

	    $status="RUNTIME_ERROR";

         } elsif (`grep "Began" $emailFil`) {
	    #   Get job start date
	    $startDate=`grep "Date: " $emailFil | tail -n 1`;
	    $startDate=~s/Date: //;
	    chomp $startDate;
	    $startDate=&formatDate($startDate);
	    #   Set status to "Running"
	    $status="RUNNING";
	    (print "startDate=$startDate\n") if ($DEBUG);
	    $val{startTime}=$startDate;
	    $val{status}=$status;
	 }
      } else {
         $scheduler = "condor";
	 if (`grep "has exited normally" $emailFil`) {
            #   Get job end date
            $endDate=`grep "Completed at:" $emailFil | tail -n 1`;
            chomp $endDate;
            $endDate=~s/Completed at:\s+//;
	    $endDate=&formatDate($endDate);
                                                                                
            #   Get Wall Time
            my $tempDate=`grep "Real Time:" $emailFil | tail -n 1`;
            chomp $tempDate;
            $tempDate=~s/Real Time:\s+//;
            my @tempDate=split /\s+/, $tempDate;
            $tempDate=$tempDate[1];
            @tempDate=split /:/, $tempDate;
            $wallTime=$tempDate[0]*3600+$tempDate[1]*60+$tempDate[2];
                                                                                
            #   Get job start date
            $dt=DateTime::Format::MySQL->parse_datetime($endDate);
            my $sec1=DateTime::Format::Epoch::Unix->format_datetime($dt);
            my $sec0=$sec1-$wallTime;
            $dt=DateTime->from_epoch(epoch =>$sec0);
            $startDate=DateTime::Format::MySQL->format_datetime($dt);
                                                                                
            #   Get CPU time. Not available for Condor so set to $walltime
            #   for now
            $cpuTime=$wallTime;
                                                                                
            #   Check if job completed successfully
            my $string=`grep -i "exited normally" $emailFil`;
            $string=~/exited normally with status ([0-9]+)/;
            my $ierr=$1;
            $status="FINISHED";
            if ($ierr > 0) {
               $status="RUNTIME_ERROR";
            }
         }
      }

      $sth = $dbh->prepare("select jobID, requestedCPUs from Jobs where localJobID like '%$localJobID%' and computeResourceID=$resourceID;") or &dbdie;
      $sth->execute() or &dbdie;
      $hash_ref=$sth->fetchrow_hashref;
      $requestedCPUs=$hash_ref->{requestedCPUs};
      $jobID=$hash_ref->{jobID};
      if ($DEBUG) {
         print "select jobID, requestedCPUs from Jobs where localJobID like \'%$localJobID%\' and computeResourceID=$resourceID\n";
	 if (!$jobID) {
	    print "NO JOBID FOUND FOR LOCALJOBID=$localJobID\n";
         } else {
            print "jobID=$jobID  requestedCPUs=$requestedCPUs\n";
	 }
      }

      ###
      ### CSG 06/28/2007
      ###
      ### Indicate if job ran out of time
      ###
            my $string=`grep -i "exceeded" $emailFil`;
            print "string=$string\n" if ($DEBUG);
            if ( $string ) {
               $status="TIME_ELAPSED";
               if ( $DEBUG) {
                  print "Job $jobID has status=$status\n";
               }
            }
            my $string=`grep -i "TERM_RUNLIMIT" $emailFil`;
            if ( $string ) {
               $status="TIME_ELAPSED";
               if ( $DEBUG) {
                  print "Job $jobID has status=$status\n";
               }
            }
            my $string=`grep -i "Exit_status=265" $emailFil`;
            if ( $string ) {
               $status="TIME_ELAPSED";
               if ( $DEBUG) {
                  print "Job $jobID has status=$status\n";
               }
            }


      $val{startTime}=$startDate if ($startDate);
      $val{stopTime}=$endDate if ($endDate);
      $val{usedWallTime}=$wallTime/3600 if ($wallTime);
      $val{usedCPUTime}=$cpuTime/3600 if ($cpuTime);
      ($val{usedCPUs}=$requestedCPUs) if ($requestedCPUs);
      $val{status}=$status;
      $val{localjobID}=$localJobID;
      $val{status}=$status;
      if (($status=~/finished/i) | ($status=~/error/i)) {
         $cost=$val{usedWallTime}*$requestedCPUs;
	 $val{cost}=$cost if ($cost);
	 ### Make sure that startTime, stopTime, usedWallTime and cpuTime are
	 ### defined
	 my @check_fields=('startTime','stopTime','usedWallTime','usedCPUTime');
	 for $i (@check_fields) {
	    if (! $val{$i}) {
	       print "$i IS NOT DEFINED\n";
	    }
	 }
      }

      my $now=localtime();
      my $lastUpdated=&formatDate($now);
      $val{lastUpdated}=$lastUpdated;

      $tableName="Jobs";
      $SQLFile="/home/ccg-admin/bin/job-updates.sql";
      $SQLString=&turnToSQL;
      $SQLString=$SQLString." where jobID=$jobID;\n";
      open(SQLFILE, " >> $SQLFile");
      if ($ENV{DEBUG}) {
         print "$SQLString\n";
      } else {
         print SQLFILE "$SQLString\n";
      }
      close(SQLFILE);
      my $finished=1;
#      if (! $ENV{DEBUG}) {
         $sth=$dbh->prepare("$SQLString") or &dbdie;
         $sth->execute() or &dbdie;
         $sth->finish();
#      }
   }
   return($finished);
   $dbh->disconnect;
}

sub turnToSQL {
   my $n=0;
   foreach ( keys (%val) ) {
      unless ($n==0) {
         $string=$string.", $_=\'$val{$_}\'";
      } else {
#      $string="Insert into $tableName SET $_=\'$val{$_}\'";
      $string="Update $tableName SET $_=\'$val{$_}\'";
      }
      $n++;
   }
   return($string);
}
