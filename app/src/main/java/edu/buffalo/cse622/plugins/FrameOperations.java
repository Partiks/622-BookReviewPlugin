package edu.buffalo.cse622.plugins;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
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

    private static final String PTAG = "PartiksTag";
    ArFragment arFragment;
    Resources dynamicResources;
    Context context;
    private HashSet<AnchorNode> pluginObjects;

    Boolean shouldAddModel=true;
    private ViewRenderable textRenderable;
    AnchorNode bookAnchor;
    Node bookInfoNode;


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

        Log.e("TEST Loading", "Constructor called");

        Session session = arFragment.getArSceneView().getSession();

        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        arFragment.getArSceneView().setupSession(session);

        setupAugmentedImagesDb(config, session);

    }

    private void processFrame(Frame frame) {
        Log.e(PTAG, "///////////////////////////////////// PROCESS FRAME IN PLUGIN CALLED");

        frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : augmentedImages) {
            if (augmentedImage.getTrackingState() == TrackingState.TRACKING) {
                //only looking for images in tracking state
                Log.e(PTAG, "/////////////////////////////////////////Augmented Image name = " + augmentedImage.getName() + " " + augmentedImage.getIndex());
                // CORE LOGIC START ----------------------->
                if(augmentedImage.getName().contains("cry")){
                    //identifying which image got recognized, more useful to match the whole image name rather than "contains"
                    // @TODO: use whole image matching instead of using conatins
                    if(bookInfoNode == null){
                        //only add if book was not recognized before
                        // @TODO: smooth out image tracking logic by looking at AugmentedImages Samples
                        Toast.makeText(context, "Detected BOOK!!", Toast.LENGTH_LONG).show();
                        bookAnchor = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                        bookInfoNode = new Node();
                        bookInfoNode.setParent(bookAnchor);
                        bookInfoNode.setRenderable(textRenderable);
                        bookInfoNode.setLocalPosition(new Vector3(0.5f * augmentedImage.getCenterPose().qx(), augmentedImage.getCenterPose().qy(), -0.5f * augmentedImage.getCenterPose().qz()));
                        bookInfoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(-1f, 0, 0), 90f));

                        TextView tv = (TextView) textRenderable.getView();

                        //getting the URL from the strings.xml values file
                        Log.e(PTAG, "ANCHOR BREAKPOINT <<<<<<<<<<>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>");
                        int stringId = dynamicResources.getIdentifier("book_who_will_cry", "string", "edu.buffalo.cse622.plugins");
                        tv.setText(dynamicResources.getText(stringId));
                        tv.setMovementMethod(LinkMovementMethod.getInstance());

                        //getting the rounded background textbox from rounded_bg.xml layout file
                        int bgId = dynamicResources.getIdentifier("rounded_bg", "drawable", "edu.buffalo.cse622.plugins");
                        Drawable background;
                        try {
                            background = Drawable.createFromXml(dynamicResources, dynamicResources.getXml(bgId));
                            tv.setBackground(background);
                            //tv.setBackgroundColor(Color.parseColor("#228B22"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //still testing out this logic, not sure this works smoothly
            if(augmentedImage.getTrackingState() == TrackingState.STOPPED){
                if(augmentedImage.getName().contains("cry")){
                    arFragment.getArSceneView().getScene().removeChild(bookAnchor);
                    arFragment.getArSceneView().getScene().removeChild(bookInfoNode);
                    bookAnchor = null;
                    bookInfoNode=null;
                }
            }
        } //end of for loop: for each recognized AugmentedImage the above for loop logic executes at least once

        if (bookAnchor != null) {
            bookAnchor.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(bookAnchor);
        }
    }

    public boolean setupAugmentedImagesDb(Config config, Session session) {
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
                int bgId = dynamicResources.getIdentifier("rounded_bg", "drawable", "edu.buffalo.cse622.plugins");
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
