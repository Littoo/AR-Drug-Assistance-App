package com.example.healthcareapp.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapp.R
import com.example.healthcareapp.models.drugs.DrugModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.example.healthcareapp.databinding.ViewRenderableInfosBinding

import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.RenderableInstance
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.model.await
import io.github.sceneview.node.ViewNode
import io.github.sceneview.utils.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.view_renderable_infos.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ARFragment: Fragment(R.layout.ar_fragment) {

    private lateinit var db: FirebaseFirestore

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton

    lateinit var cursorNode: CursorNode
    lateinit var modelNode: ArModelNode
    lateinit var viewNode: ViewNode

    lateinit var drugInfoView: View
    lateinit var drugNameText: TextView

//    private var viewRenderable: ViewRenderable? = null

    var drug: String = "Dapagliflozin"

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
            actionButton.isGone = value
        }

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
////        drugInfoView = inflater.inflate(R.layout.view_renderable_infos, container, false)
////        drugNameText = drugInfoView.findViewById(R.id.drugName)
////        Toast.makeText(context, "I am Here inititalized: " + drugNameText.text.toString(),Toast.LENGTH_SHORT).show()
////        drugNameText.text = "Olaalalalala"
////        Toast.makeText(context, "I am Here inititalized: " + drugNameText.text.toString(),Toast.LENGTH_SHORT).show()
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            db = FirebaseFirestore.getInstance()

            var text: String = loadDrugData(drug)
            val root = ViewRenderableInfosBinding.inflate(layoutInflater)
//        root.drugContent.drugName.text = text
            root.drugName.text = text

            loadingView = view.findViewById(R.id.loadingView)
            actionButton = view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
                val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                doOnApplyWindowInsets { systemBarsInsets ->
                    (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                        systemBarsInsets.bottom + bottomMargin
                }
                setOnClickListener {
                    cursorNode.createAnchor()?.let { anchorOrMove(it) }
                }
            }

            sceneView = view.findViewById<ArSceneView?>(R.id.sceneView).apply {
                planeRenderer.isVisible = false
                // Handle a fallback in case of non AR usage. The exception contains the failure reason
                // e.g. SecurityException in case of camera permission denied
                onArSessionFailed = { _: Exception ->
                    // If AR is not available or the camara permission has been denied, we add the model
                    // directly to the scene for a fallback 3D only usage
//                modelNode.centerModel(origin = Position(x = 0.0f, y = 0.0f, z = 4.0f))
//                modelNode.scaleModel(units = 1.0f)
//                sceneView.addChild(modelNode)
                }
                onTouchAr = { hitResult, _ ->
                    anchorOrMove(hitResult.createAnchor())
//                    text = loadDrugData(drug)
                    Log.i(" View Rendered: ", text.toString())
                    root.drugName.text = text
                    val text2 = root.root.findViewById<TextView>(R.id.drugName).text
                    Log.i(" View Rendered: ", text2.toString())
                }
            }

            cursorNode = CursorNode(
                context = requireContext(),
                lifecycle = lifecycle ).apply {
                onTrackingChanged = { _, isTracking, _ ->
                    if (!isLoading) {
                        actionButton.isGone = !isTracking
                    }
                }
            }
            sceneView.addChild(cursorNode)

//        isLoading = true

            viewNode = ViewNode(
                position = Position(0.0f, 1f, 0.0f),
                scale = Scale(0.7f)
            )

            viewNode.loadCustomView(
                context = requireContext(),
                lifecycle = lifecycle,
                layout = root,
            )

            modelNode = ArModelNode()
            modelNode.loadModelAsync(context = requireContext(),
                lifecycle = lifecycle,
                glbFileLocation = "models/halloween.glb",
//            centerOrigin =  Position(x = 0.0f, y = 0.0f, z = 0.0f),
                onLoaded = {it->
                    actionButton.text = getString(R.string.move_object)
                    actionButton.setIconResource(R.drawable.ic_target)
                    isLoading = false

                })
        }


    }

    fun anchorOrMove(anchor: Anchor) {
        if (!sceneView.children.contains(modelNode)) {
            sceneView.addChild(modelNode).apply {
                addChild(viewNode)
            }
        }
        modelNode.anchor = anchor
    }

    private suspend fun getDataFromFirestore(gen_name: String): DocumentSnapshot{

        var ref = db.collection("drugs")
        val snapshot = ref.whereEqualTo("generic_name", gen_name)
            .get()
            .await()
        return snapshot.documents.first()
    }

    private suspend fun  loadDrugData(gen_name: String): String{
        lateinit var drug: DrugModel
        try {
            val drugData = getDataFromFirestore(gen_name)
            drug = drugData.toObject(DrugModel::class.java)!!

        } catch (e: Exception) {
            Log.d("Error loading Data", e.toString()) //Don't ignore potential errors!
        }
        return drug.generic_name
    }
//    private fun imageFromBitmap(bitmap: Bitmap): InputImage{
//        return InputImage.fromBitmap(bitmap, 0)
//    }
//
//
//    private fun detectText(bitmap: Bitmap){
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//        val image = imageFromBitmap(bitmap)
//
//        recognizer.process(image)
//            .addOnSuccessListener {
//                binding.tvResult.setText(it.text)
//                textResult = it.text
//                binding.btnSave.isEnabled = true
//                binding.btnCopy.isEnabled = true
//                binding.tvResult.isEnabled = true
//                Toast.makeText(this, "Teks berhasil dideteksi", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//                Toast.makeText(this, "Gagal Mendeteksi Teks", Toast.LENGTH_SHORT).show()
//            }
//    }
}

private fun ViewNode.loadCustomView(
    context: Context,
    lifecycle: Lifecycle? = null,
    layout: ViewRenderableInfosBinding,
    onError: ((error: Exception) -> Unit)? = null,
    onLoaded: ((instance: RenderableInstance, view: View) -> Unit)? = null) {
    if (lifecycle != null) {
        lifecycle.coroutineScope.launchWhenCreated {
            try {
                val renderable = ViewRenderable.builder()
                    .setView(context, layout.root)
                    .await(lifecycle)
                val view = renderable.view
                val instance = setRenderable(renderable)
                onLoaded?.invoke(instance!!, view)
                onViewLoaded(instance!!, view)
            } catch (error: java.lang.Exception) {
                onError?.invoke(error)
                onError(error)
            }
        }
    } else {
        doOnAttachedToScene { scene ->
            loadCustomView(context, scene.lifecycle, layout, onError, onLoaded)
        }
    }
}
