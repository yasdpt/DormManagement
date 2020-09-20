package ir.staryas.dormmanagement.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import ir.staryas.dormmanagement.R;

public class ViewDialog {

    private Context context;
    private Dialog loadingDialog;
    //..we need the context else we can not create the dialog so get context in constructor
    public ViewDialog(Context context) {
        this.context = context;
    }

    public void showDialog() {

        loadingDialog  = new Dialog(context);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //...set cancelable false so that it's never get hidden
        loadingDialog.setCancelable(false);
        //...that's the layout i told you will inflate later
        loadingDialog.setContentView(R.layout.custom_dialog);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);



        //...finally show it
        loadingDialog.show();
    }

    //..also create a method which will hide the dialog when some work is done
    public void hideDialog(){
        loadingDialog.dismiss();
    }



}
