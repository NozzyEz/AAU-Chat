package org.nozzy.android.AAU_Chat.Email;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;


//Class is extending AsyncTask because this class is going to perform a networking operation
public class DeleteMail extends AsyncTask<Void,Void,Void> {

    //Declaring Variables
    private Context context;
    private Session session;

    //Information to send email
    private String email;
    private String subject;
    private String message;

    //Progressdialog to show while sending email
    private ProgressDialog progressDialog;

    //Class Constructor
    public DeleteMail(Context context, String subject) {
        //Initializing variables
        this.context = context;
        this.subject = subject;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Showing progress dialog while sending email
        progressDialog = ProgressDialog.show(context, "Deleting message..", "Please wait...", false, false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //Dismissing the progress dialog
        progressDialog.dismiss();
        //Showing a success message
        Toast.makeText(context, "Message deleted", Toast.LENGTH_LONG).show();
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
        session = Session.getDefaultInstance(props,
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

                    if (folder.getMessageCount() > 1) {
                        folder.open(folder.READ_WRITE);

                        Mail newMail;

                        for (javax.mail.Message message : folder.getMessages()) {

                            if (message.getSubject().equals(subject)) {
                                message.setFlag(Flags.Flag.DELETED, true);
                                break;
                            }

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
}