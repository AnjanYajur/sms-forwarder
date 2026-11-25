# SMS Forwarder (Android)

Emails any incoming SMS containing a keyword (default `SBM`, case-insensitive)
to an email address, automatically, in the background.

## Build
Push to `main` and download the `sms-forwarder-apk` artifact from the
GitHub Actions run, or open the folder in Android Studio and run it.

## Set up
1. Create a Gmail **app password**: Google Account > Security > turn on 2-Step
   Verification > App passwords. (Your normal Gmail password will not work.)
2. In the app enter the destination email, your Gmail address, the app password
   and the keyword, tap **Save**, then turn on **Forwarding enabled** and allow
   the SMS permission.

The email subject is `SMS from <sender>` and the body is the original message.

## Notes
- Android only; iOS does not allow this.
- The phone needs internet access; each email is retried up to 3 times.
- The app password is stored unencrypted in the app's private storage. Use a
  dedicated Gmail account and revoke the app password if you lose the phone.
- Some phones (Xiaomi, Oppo, Vivo, Samsung) kill background apps: set the app's
  battery to "Unrestricted" / allow autostart. If sideloaded on Android 13+, you may
  need App info > three dots > "Allow restricted settings" before SMS permission can be granted.
