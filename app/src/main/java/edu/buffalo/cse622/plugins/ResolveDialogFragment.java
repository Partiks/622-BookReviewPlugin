/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.buffalo.cse622.plugins;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/** A DialogFragment for the Resolve Dialog Box. */
public class ResolveDialogFragment extends DialogFragment {
  private static final String PTAG = "PartiksTag";
  Activity activity;
  Context context;

  // The maximum number of characters that can be entered in the EditText.
  private static final int MAX_FIELD_LENGTH = 6;

  /** Functional interface for getting the value entered in this DialogFragment. */
  public interface OkListener {
    /**
     * This method is called by the dialog box when its OK button is pressed.
     *
     * @param dialogValue the long value from the dialog box
     */
    void onOkPressed(int dialogValue);
  }

  public static ResolveDialogFragment createWithOkListener(OkListener listener) {
    ResolveDialogFragment frag = new ResolveDialogFragment();
    //frag.okListener = listener;
    return frag;
  }

  private EditText shortCodeField;
  //private OkListener okListener;

  /*@Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder
        .setView(createDialogLayout())
        .setTitle("Resolve Anchor")
        .setPositiveButton("Resolve", (dialog, which) -> onResolvePressed())
        .setNegativeButton("Cancel", (dialog, which) -> {});
    return builder.create();
  } */

  /*public static ResolveDialogFragment onPartiksCreate1() {
    ResolveDialogFragment frag = new ResolveDialogFragment();
    return frag;
  } */
  //unnecessary dialog box cerating code here

  public void partiksSetup(Activity activity2, Context context2){
    this.activity = activity2;
    this.context = context2;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Log.e(PTAG, "<<<<<<<<<<<<<<<<<<<<<<<< ON CREATE DIALOG CALLED >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    Dialog d = onPartiksCreate();
    return d;
  }

  public Dialog onPartiksCreate(){
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder
            .setView(createDialogLayout(context))
            .setTitle("Resolve Anchor")
            .setPositiveButton("Resolve", (dialog, which) -> onResolvePressed())
            .setNegativeButton("Cancel", (dialog, which) -> {});
    return builder.create();
  }

  private LinearLayout createDialogLayout(Context context) {
    LinearLayout layout = new LinearLayout(context);
    shortCodeField = new EditText(context);
    // Only allow numeric input.
    shortCodeField.setInputType(InputType.TYPE_CLASS_NUMBER);
    shortCodeField.setLayoutParams(
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    // Set a max length for the input text to avoid overflows when parsing.
    shortCodeField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_FIELD_LENGTH)});
    layout.addView(shortCodeField);
    layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    return layout;
  }

  private void onResolvePressed() {
    Editable roomCodeText = shortCodeField.getText();
    /*if (okListener != null && roomCodeText != null && roomCodeText.length() > 0) {
      int longVal = Integer.parseInt(roomCodeText.toString());
      okListener.onOkPressed(longVal);
    } */
  }
}
