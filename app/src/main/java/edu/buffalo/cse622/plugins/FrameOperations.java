package edu.buffalo.cse622.plugins;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;

import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

public class FrameOperations {

    private static final String TAG = "BookReviewPlugin:";
    ArFragment arFragment;
    Resources dynamicResources;
    Context context;
    private HashSet<AnchorNode> pluginObjects;

    Boolean shouldAddModel=true;
    private ViewRenderable textRenderable;
    private ViewRenderable textRenderable2;
    private ViewRenderable textRenderable3;

    AnchorNode bookAnchor;
    Node bookInfoNode;
    AnchorNode sreBookAnchor;
    Node sreBookInfoNode;
    AnchorNode thrillerBookAnchor;
    Node thrillerBookInfoNode;


    public FrameOperations(Resources dynamicResources2, ArFragment arFragment2, HashSet<AnchorNode> pluginObjects){
        this.arFragment = arFragment2;
        context=arFragment.getContext();
        dynamicResources = dynamicResources2;
        this.pluginObjects = pluginObjects;

        int layoutId = dynamicResources.getIdentifier("text_view", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml = dynamicResources.getLayout(layoutId);
        View view = LayoutInflater.from(context).inflate(textViewXml, null);

        layoutId = dynamicResources.getIdentifier("text_view2", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml2 = dynamicResources.getLayout(layoutId);
        View view2 = LayoutInflater.from(context).inflate(textViewXml2, null);

        layoutId = dynamicResources.getIdentifier("text_view3", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml3 = dynamicResources.getLayout(layoutId);
        View view3 = LayoutInflater.from(context).inflate(textViewXml3, null);

        ViewRenderable.builder()
                .setView(context, view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            textRenderable = renderable;
                        }
                );

        ViewRenderable.builder()
                .setView(context, view2)
                .build()
                .thenAccept(
                        (renderable) -> {
                            textRenderable2 = renderable;
                        }
                );

        ViewRenderable.builder()
                .setView(context, view3)
                .build()
                .thenAccept(
                        (renderable) -> {
                            textRenderable3 = renderable;
                        }
                );

        Session session = arFragment.getArSceneView().getSession();

        Config config = new Config(session);
        config.setFocusMode(Config.FocusMode.AUTO);
        session.configure(config);
        arFragment.getArSceneView().setupSession(session);

        setupAugmentedImagesDb(config, session);

    }

    private void processFrame(Frame frame) {

        frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        // who will cry when you die book height: 17cm and width: 12 cm
        // sre book width: ~18 cm and height = ~23 cms
        // thriller book: width = 14 cm and height = 20.5 cms
        for (AugmentedImage augmentedImage : augmentedImages) {
            switch (augmentedImage.getTrackingState()){
                case TRACKING:
                    if(augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING){

                        if(augmentedImage.getName().equals("who-will-cry-when-you-die")){
                            // enable book card node
                            if(bookInfoNode == null){
                                //only add if book was not recognized before

                                float imageWidth = 0.12f;
                                float imageHeight = 0.18f;

                                float scaledWidth = imageWidth/augmentedImage.getExtentX();
                                float scaledHeight = imageHeight/augmentedImage.getExtentZ();

                                bookAnchor = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                                bookInfoNode = new Node();
                                bookInfoNode.setParent(bookAnchor);
                                textRenderable.setShadowCaster(false);
                                textRenderable.setShadowReceiver(false);
                                bookInfoNode.setRenderable(textRenderable);
                                bookInfoNode.setLocalScale(new Vector3(scaledWidth/4, scaledHeight/4, scaledWidth/4));
                                bookInfoNode.setLocalPosition(new Vector3(0.01f*augmentedImage.getCenterPose().qx(), 0.25f* augmentedImage.getCenterPose().qy(), 1.8f*augmentedImage.getCenterPose().qz()));
                                bookInfoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(-1f, 0, 0), 90f));

                                TextView tv = (TextView) textRenderable.getView();

                                //getting the text content and URL from the strings.xml values file
                                int stringId = dynamicResources.getIdentifier("book_who_will_cry", "string", "edu.buffalo.cse622.plugins");
                                tv.setText(dynamicResources.getText(stringId));
                                tv.setMovementMethod(LinkMovementMethod.getInstance());
                                tv.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

                                //getting the rounded background textbox from green_rounded_bg.xml layout file
                                int bgId = dynamicResources.getIdentifier("green_rounded_bg", "drawable", "edu.buffalo.cse622.plugins");
                                Drawable background;
                                try {
                                    background = Drawable.createFromXml(dynamicResources, dynamicResources.getXml(bgId));
                                    tv.setBackground(background);
                                    //tv.setBackgroundColor(Color.parseColor("#228B22"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }else if(augmentedImage.getName().equals("sre-book")){

                            // enable sre book card node
                            if(sreBookInfoNode == null){
                                //only add if book was not recognized before

                                float imageWidth = 0.19f;
                                float imageHeight = 0.23f;

                                float scaledWidth = imageWidth/augmentedImage.getExtentX();
                                float scaledHeight = imageHeight/augmentedImage.getExtentZ();

                                sreBookAnchor = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                                sreBookInfoNode = new Node();
                                sreBookInfoNode.setParent(sreBookAnchor);
                                textRenderable2.setShadowCaster(false);
                                textRenderable2.setShadowReceiver(false);
                                sreBookInfoNode.setRenderable(textRenderable2);
                                sreBookInfoNode.setLocalScale(new Vector3(scaledWidth/4, scaledHeight/4, scaledWidth/4));
                                sreBookInfoNode.setLocalPosition(new Vector3(0.01f*augmentedImage.getCenterPose().qx(), 0.25f* augmentedImage.getCenterPose().qy(), 1.8f*augmentedImage.getCenterPose().qz()));
                                sreBookInfoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(-1f, 0, 0), 90f));

                                TextView tv = (TextView) textRenderable2.getView();

                                //getting the text content and URL from the strings.xml values file
                                int stringId = dynamicResources.getIdentifier("book_sre", "string", "edu.buffalo.cse622.plugins");
                                tv.setText(dynamicResources.getText(stringId));
                                tv.setMovementMethod(LinkMovementMethod.getInstance());
                                tv.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

                                //getting the rounded background textbox from green_rounded_bg.xml layout file
                                int bgId = dynamicResources.getIdentifier("blue_rounded_bg", "drawable", "edu.buffalo.cse622.plugins");
                                Drawable background;
                                try {
                                    background = Drawable.createFromXml(dynamicResources, dynamicResources.getXml(bgId));
                                    tv.setBackground(background);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if(augmentedImage.getName().equals("those-we-left-behind")){
                            if(thrillerBookInfoNode == null){
                                //only add if book was not recognized before
                                Log.e(TAG, "THRILLER BOOK ADDING NODE<<<<<<<<<<>>>>>>>>>");
                                float imageWidth = 0.14f;
                                float imageHeight = 0.205f;

                                float scaledWidth = imageWidth/augmentedImage.getExtentX();
                                float scaledHeight = imageHeight/augmentedImage.getExtentZ();

                                thrillerBookAnchor = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                                thrillerBookInfoNode = new Node();
                                thrillerBookInfoNode.setParent(thrillerBookAnchor);
                                textRenderable3.setShadowCaster(false);
                                textRenderable3.setShadowReceiver(false);
                                thrillerBookInfoNode.setRenderable(textRenderable3);
                                thrillerBookInfoNode.setLocalScale(new Vector3(scaledWidth/6, scaledHeight/10, scaledWidth/6));
                                thrillerBookInfoNode.setLocalPosition(new Vector3(0.01f*augmentedImage.getCenterPose().qx(), 0.01f* augmentedImage.getCenterPose().qy(), 0.01f*augmentedImage.getCenterPose().qz()));
                                thrillerBookInfoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(-1f, 1f, 1f), 90f));

                                TextView tv = (TextView) textRenderable3.getView();

                                //getting the text content and URL from the strings.xml values file
                                int stringId = dynamicResources.getIdentifier("book_those_we_left_behind", "string", "edu.buffalo.cse622.plugins");
                                tv.setText(dynamicResources.getText(stringId));
                                tv.setMovementMethod(LinkMovementMethod.getInstance());
                                tv.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

                                //getting the rounded background textbox from green_rounded_bg.xml layout file
                                int bgId = dynamicResources.getIdentifier("red_rounded_bg", "drawable", "edu.buffalo.cse622.plugins");
                                Drawable background;
                                try {
                                    background = Drawable.createFromXml(dynamicResources, dynamicResources.getXml(bgId));
                                    tv.setBackground(background);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }else{
                        // remove this object's anchor node
                        if(augmentedImage.getName().equals("who-will-cry-when-you-die") && bookAnchor != null){
                            //arFragment.getArSceneView().getScene().removeChild(bookInfoNode);
                            bookInfoNode=null;
                            arFragment.getArSceneView().getScene().removeChild(bookAnchor);
                            bookAnchor=null;
                        }
                        if(augmentedImage.getName().equals("sre-book") && sreBookAnchor != null){
                            //arFragment.getArSceneView().getScene().removeChild(sreBookInfoNode);
                            arFragment.getArSceneView().getScene().removeChild(sreBookAnchor);
                            sreBookInfoNode=null;
                            sreBookAnchor=null;
                        }
                        if(augmentedImage.getName().equals("those-we-left-behind") && thrillerBookAnchor != null){
                            //arFragment.getArSceneView().getScene().removeChild(thrillerBookInfoNode);
                            arFragment.getArSceneView().getScene().removeChild(thrillerBookAnchor);
                            thrillerBookInfoNode=null;
                            thrillerBookAnchor=null;
                        }
                    }
            }

        } //end of for loop: for each recognized AugmentedImage the above for loop logic executes at least once

        if (bookAnchor != null) {
            bookAnchor.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(bookAnchor);
        }
        if (sreBookAnchor != null) {
            sreBookAnchor.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(sreBookAnchor);
        }
        if(thrillerBookAnchor!=null){
            thrillerBookAnchor.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(thrillerBookAnchor);
        }
    }

    private void planeTap(HitResult hitResult) {

    }

    private void onDestroy() {

    }

    public boolean setupAugmentedImagesDb(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;
        try {
            //new way of loading things without the integer ID
            InputStream is = dynamicResources.getAssets().open("partiks_books_img_database.imgdb");
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO exception loading augmented image database.", e);
            return false;
        }

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        //printing number of Augmented Images inside the DB to verify if everything is working fine.
        Log.e(TAG, "INSIDE APK SESSION DB CONTENTS = " + augmentedImageDatabase.getNumImages());

        session.configure(config);

        return true;
    }

}
