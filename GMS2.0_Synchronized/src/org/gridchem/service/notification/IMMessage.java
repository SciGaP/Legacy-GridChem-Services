/*Copyright (c) 2004,University of Illinois at Urbana-Champaign.  All rights reserved.
 * 
 * Created on May 10, 2007
 * 
 * Developed by: CCT, Center for Computation and Technology, 
 * 				NCSA, University of Illinois at Urbana-Champaign
 * 				OSC, Ohio Supercomputing Center
 * 				TACC, Texas Advanced Computing Center
 * 				UKy, University of Kentucky
 * 
 * https://www.gridchem.org/
 * 
 * Permission is hereby granted, free of charge, to any person 
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal with the Software without 
 * restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom 
 * the Software is furnished to do so, subject to the following conditions:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimers.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimers in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the names of Chemistry and Computational Biology Group , NCSA, 
 *    University of Illinois at Urbana-Champaign, nor the names of its contributors 
 *    may be used to endorse or promote products derived from this Software without 
 *    specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS WITH THE SOFTWARE.
*/

package org.gridchem.service.notification;


import org.apache.log4j.Logger;
import org.gridchem.service.beans.UserBean;
import org.gridchem.service.exceptions.NotificationException;
import org.gridchem.service.model.Notification;

/**
 * Simple IM class using the jaim API to send an instant message to the 
 * user using their contact information from the DB.  This protocol only 
 * supports AOL Instant Messanger right now, so users will have to have
 * valid AOL accounts or this won't work.  
 * 
 * Good times to send notification would be when their account is activated, 
 * job start, stop, and migrations.
 * 
 * @author Rion Dooley < dooley [at] tacc [dot] utexas [dot] edu >
 *
 */
public class IMMessage {

    public static Logger log = Logger.getLogger(IMMessage.class.getName());

    public static void send(UserBean user, String message) throws NotificationException {
        
        
        log.debug("Sending message to " + user.getFirstName() + 
                " " + user.getLastName() + " at " + 
                user.getIm());
        System.out.println("Sending message to " + user.getFirstName() + 
                " " + user.getLastName() + " at " + 
                user.getIm());
       
        
    }
    
    public static void send(UserBean user, Notification notification) throws NotificationException {
    
        log.debug("Sending message to " + user.getFirstName() + 
                " " + user.getLastName() + " at " + 
                user.getIm());
        
        try {
            
        } catch (Exception e) {
            log.error(e);
            throw new NotificationException("IM Notification failed",e);
        }
        
        
    }
    
    //  ********************** Utility Methods ********************** //
    
}
