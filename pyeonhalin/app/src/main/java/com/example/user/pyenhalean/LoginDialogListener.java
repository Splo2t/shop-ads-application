package com.example.user.pyenhalean;



public interface LoginDialogListener {
   void onPositiveClicked(String id, String pw, String key, String cookie, String type);
   void onNegativeClicked();
}