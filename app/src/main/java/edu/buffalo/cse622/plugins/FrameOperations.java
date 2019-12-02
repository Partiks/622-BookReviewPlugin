package edu.buffalo.cse622.plugins;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private static final String TAG = "BookReviewPlugin:" + FrameOperations.class.getSimpleName();
    ArFragment arFragment;
    Resources dynamicResources;
    Context context;
    private HashSet<AnchorNode> pluginObjects;

    Boolean shouldAddModel=true;
    private ViewRenderable textRenderable;
    private ViewRenderable textRenderable2;
    AnchorNode bookAnchor;
    Node bookInfoNode;
    AnchorNode sreBookAnchor;
    Node sreBookInfoNode;


    public FrameOperations(Resources dynamicResources2, ArFragment arFragment2, HashSet<AnchorNode> pluginObjects){
        this.arFragment = arFragment2;
        context=arFragment.getContext();
        //context = arFragment.getContext();
        dynamicResources = dynamicResources2;
        this.pluginObjects = pluginObjects;

        int layoutId = dynamicResources.getIdentifier("text_view", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml = dynamicResources.getLayout(layoutId);
        View view = LayoutInflater.from(context).inflate(textViewXml, null);

        layoutId = dynamicResources.getIdentifier("text_view2", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml2 = dynamicResources.getLayout(layoutId);
        View view2 = LayoutInflater.from(context).inflate(textViewXml2, null);

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

        Log.e("TEST Loading", "Constructor called");

        Session session = arFragment.getArSceneView().getSession();
        Config config = session.getConfig();
        setupAugmentedImagesDb(config, session);
    }

    private void processFrame(Frame frame) {
        //Log.e(PTAG, "///////////////////////////////////// PROCESS FRAME IN PLUGIN CALLED");

        frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        // who will cry when you die book height: 18cm and width: 12 cm
        // sre book width: ~19 cm and height = ~23 cms
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

                                Toast.makeText(context, "Detected BOOK!!", Toast.LENGTH_LONG).show();
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
                            //Log.e(PTAG, "SRE BOOK DETECTED <<<<<<<<<>>>>>>>>>>><<<<<<<<<<>>>>>>>>>>>>>");
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
                                Toast.makeText(context, "Configured Book Node", Toast.LENGTH_LONG);

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
                        else if(augmentedImage.getName().equals("earth")){

                        }

                    }else{
                        // remove this object's anchor node
                        if(augmentedImage.getName().equals("who-will-cry-when-you-die")){
                            arFragment.getArSceneView().getScene().removeChild(bookInfoNode);
                            bookInfoNode=null;
                            arFragment.getArSceneView().getScene().removeChild(bookAnchor);
                            bookAnchor=null;
                        }
                        if(augmentedImage.getName().equals("sre-book")){
                            arFragment.getArSceneView().getScene().removeChild(sreBookInfoNode);
                            arFragment.getArSceneView().getScene().removeChild(sreBookAnchor);
                            sreBookInfoNode=null;
                            sreBookAnchor=null;
                        }
                        if(augmentedImage.getName().equals("earth")){

                        }
                    }
            }

        } //end of for loop: for each recognized AugmentedImage the above for loop logic executes at least once

        if (bookAnchor != null) {
            bookAnchor.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(bookAnchor);
        }
        if (sreBookAnchor != null) {
            Log.e(TAG, "ADDED SRE BOOK TO THE SCENE");
            Toast.makeText(context, "ADDED SRE Book To Scene", Toast.LENGTH_LONG);
            sreBookAnchor.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(sreBookAnchor);
        }
    }

    private void planeTap(HitResult hitResult) {
    }

    private void onDestroy() {
    }

    public boolean setupAugmentedImagesDb(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase = config.getAugmentedImageDatabase();

        Bitmap bitmap1 = loadAugmentedImage("who-will-cry-when-you-die.jpg");
        if (bitmap1 == null) {
            return false;
        }

        augmentedImageDatabase.addImage("who-will-cry-when-you-die", bitmap1);
        config.setAugmentedImageDatabase(augmentedImageDatabase);

        Bitmap bitmap2 = loadAugmentedImage("sre-book.jpg");
        if (bitmap2 == null) {
            return false;
        }

        augmentedImageDatabase.addImage("sre-book", bitmap2);
        config.setAugmentedImageDatabase(augmentedImageDatabase);

        session.configure(config);

        return true;

        /*
        // CORE LOGIC START ----------------------->
        AugmentedImageDatabase augmentedImageDatabase;
        try {
            //new way of loading things without the integer ID
            InputStream is = dynamicResources.getAssets().open("partiks_books_img_database.imgdb");
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(PTAG, "IO exception loading augmented image database.", e);
            return false;
        }

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        // CORE LOGIC OVER ----------------------->
        //printing number of Augmented Images inside the DB to verify if everything is working fine.
        Log.e(PTAG, "INSIDE APK SESSION DB CONTENTS = " + augmentedImageDatabase.getNumImages());
        //Toast.makeText(context, "INSIDE APK SESSION DB CONTENTS = " + augmentedImageDatabase.getNumImages(), Toast.LENGTH_SHORT).show();

        session.configure(config);

        return true;
        */
    }

    private Bitmap loadAugmentedImage(String fileName) {
        try {
            AssetManager assetManager = dynamicResources.getAssets();
            InputStream is = assetManager.open(fileName);
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "loadAugmentedImage: " + "IO Exception", e);
        }
        return null;
    }
// -------------- BOX OPERATIONS BASIC TEST PLUGINS -----------------
// BELOW CODE NOT USED IN BOOK REVIEW PLUGIN
    /*public FrameOperations( Resources dynamicResources2, ArFragment arFragment2){

        context=arFragment2.getContext();
        //context = arFragment.getContext();
        dynamicResources = dynamicResources2;

        this.arFragment = arFragment2;
        // This is how we load a layout resource.
        int layoutId = dynamicResources.getIdentifier("text_view", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml = dynamicResources.getLayout(layoutId);
        View view = LayoutInflater.from(context).inflate(textViewXml, null);


        ViewRenderable.builder()
                .setView(context, view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            textRenderable = renderable;
                        }
                );
        node = new Node();

        Log.e("TEST Loading", "Constructor called");
        Toast.makeText(context, "New APK!", Toast.LENGTH_LONG).show();
    }

    private Node processFrame(Frame frame) {
        for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            if(plane.getTrackingState() == TrackingState.TRACKING ) {
                Anchor anchor = plane.createAnchor(plane.getCenterPose());
                AnchorNode anchorNode = new AnchorNode(anchor);
                node = new Node();
                node.setParent(anchorNode);
                node.setRenderable(textRenderable);
                node.setLocalPosition(new Vector3(-0.5f*plane.getCenterPose().qx()+0.15f, -2f*plane.getCenterPose().qy(), -2f * plane.getCenterPose().qz() ));
                node.setLocalRotation(Quaternion.axisAngle(new Vector3(-1f, 0, 0), 90f));

                TextView tv = (TextView) textRenderable.getView();

                //tv.setBackgroundColor(Color.parseColor("#0000FF"));
                //#228B22
                int stringId = dynamicResources.getIdentifier("partiks_hello", "string", "edu.buffalo.cse622.plugins");
                tv.setText(dynamicResources.getString(stringId));
                int bgId = dynamicResources.getIdentifier("green_rounded_bg", "drawable", "edu.buffalo.cse622.plugins");
                Drawable background;
                try {
                    background = Drawable.createFromXml(dynamicResources, dynamicResources.getXml(bgId));
                    tv.setBackground(background);
                    tv.setBackgroundColor(Color.parseColor("#228B22"));
                }catch(Exception e){
                    e.printStackTrace();
                }
                //tv.setBackgroundResource(bgId);


                //-tv.setText(context.getString(R.string.partiks_hello));


                break;
            }
        }

        return node;
    } */
}
