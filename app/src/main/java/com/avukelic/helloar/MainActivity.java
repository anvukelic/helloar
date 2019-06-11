package com.avukelic.helloar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    public static final String TAG = "MainActivity";

    private ArFragment arFragment;
    private Plane plane;
    private double degree;
    private Anchor modelAnchor;
    private ViewRenderable model;
    private AnchorNode anchorNode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ViewRenderable.builder()
                .setView(this, R.layout.pin_model)
                .build()
                .thenAccept(viewRenderable -> model = viewRenderable);
        onUpdateRender();
    }

    private void onUpdateRender() {
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
    }


    public void onUpdate(FrameTime frameTime) {
        if (arFragment.getArSceneView().getArFrame() == null) {
            Log.d(TAG, "onUpdate: No frame available");
            // No frame available
            return;
        }

        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);

        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState()
                != TrackingState.TRACKING) {
            Log.d(TAG, "onUpdate: Tracking not started yet");
            anchorNode = null;
            // Tracking not started yet
            return;
        }

        if (anchorNode == null && model != null) {
            Log.d(TAG, "onUpdate: mAnchorNode is null");
            Session session = arFragment.getArSceneView().getSession();

            Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
            Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(1.0f));

            // Create an ARCore Anchor at the position.
            int r = 6378137;
            float lat = (float) 45.560001;
            float lng = (float) 18.675880;
            float x = (float) (r * Math.cos(lat) * Math.cos(lng));
            float y = (float) (r * Math.sin(lat));
            float z = (float) (r * Math.cos(lat) * Math.sin(lng));
            Pose pose = Pose.makeTranslation(x, y,z);
            //Pose pose2 = Pose.makeTranslation(position.x - 0.5f, position.y, position.z - 0.5f);
            Anchor anchor = session.createAnchor(pose);
            //Anchor anchor2 = session.createAnchor(pose2);

            anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            //TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
            Node transformableNode = new Node();
            transformableNode.setRenderable(model);
            transformableNode.setOnTouchListener(new Node.OnTouchListener() {
                @Override
                public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    Toast.makeText(MainActivity.this, "Test", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            transformableNode.setParent(anchorNode);
            /*anchorNode = new AnchorNode(anchor2);
            anchorNode.setParent(arFragment.getArSceneView().getScene());*/

            /*Node transformableNode2 = new Node();
            transformableNode2.setRenderable(model);
            transformableNode2.setParent(anchorNode);*/
        }
    }
}


