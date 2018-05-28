package org.nozzy.android.AAU_Chat.Email;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;


//Class is extending AsyncTask because this class is going to perform a networking operation
public class GetMails extends AsyncTask<Void,Void,Void> {


    //Declaring Variables
    private Context context;
    private Session session;


    private ProgressDialog progressDialog;

    public GetMails(Context context){
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);


        //Showing a success message
        Toast.makeText(context,"E-mails synced!",Toast.LENGTH_LONG).show();

    }

    @Override
    protected Void doInBackground(Void... params) {
        //Creating properties
        Properties props = new Properties();

        //Configuring properties for aau mail
        props.put("mail.smtp.host", "mail.aau.dk");
        props.put("mail.smtp.socketFactory.port", "993");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "993");

        //Creating a new session
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                    }
                });



        try {

            javax.mail.Store store = session.getStore("imaps");
            store.connect("mail.aau.dk", Config.EMAIL, Config.PASSWORD);
            javax.mail.Folder[] folders = store.getDefaultFolder().list("*");


            for (javax.mail.Folder folder : folders) {

                if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
                    //Config.listFolders.add(folder.getName());

                    if(folder.getMessageCount() > 1) {
                        folder.open(folder.READ_ONLY);

                        Mail newMail;

                        for (javax.mail.Message message : folder.getMessages()) {

                            newMail = new Mail( String.valueOf(Config.listMails.size()+1));

                            // Get the folder the mail is in
                            newMail.setFolder(folder.getName());

                            // Get Time Stamp
                            newMail.setTimeStamp(message.getReceivedDate().toString());

                            // Get read statues:
                            if(message.isSet(Flags.Flag.SEEN)) {
                                newMail.setIsRead("1");
                            }
                            else {
                                newMail.setIsRead("0");
                            }

                            // Get from:
                            newMail.setFrom(message.getFrom()[0].toString());

                            // Get subject:
                            newMail.setSubject(message.getSubject());

                            // Get content:
                            newMail.setMessage(getTextFromMessage(message));

                            // Add new mail to the list
                            Config.listMails.add(newMail);

                        }
                    }
                }


            }





            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }



    /**
     * Return the primary text content of the message.
     */
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }
}