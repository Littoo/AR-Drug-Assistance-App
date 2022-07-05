package com.example.healthcareapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.*
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.healthcareapp.Constants
import com.example.healthcareapp.R
import com.example.healthcareapp.models.drugs.DrugModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.example.healthcareapp.databinding.ViewRenderableInfosBinding
import com.example.healthcareapp.databinding.ArFragmentBinding
import com.example.healthcareapp.databinding.ViewRenderableNoteInfosBinding
import com.example.healthcareapp.databinding.ViewRenderablePhotoBinding
//import com.example.healthcareapp.ObjectDetector

import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.TrackingFailureReason

import com.google.ar.sceneform.rendering.RenderableInstance
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.objects.DetectedObject
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.ar.node.EditableTransform
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.await
import io.github.sceneview.node.ViewNode
import io.github.sceneview.utils.doOnApplyWindowInsets
import kotlinx.coroutines.*
//import kotlinx.android.synthetic.main.view_renderable_infos.view.*
import kotlinx.coroutines.tasks.await
import java.io.File

typealias IdAnalyzer = (detectedObject: DetectedObject) -> Unit

class ARFragment: Fragment(R.layout.ar_fragment) {

    private lateinit var db: FirebaseFirestore

    private val TAG = "AR Fragment"

    private lateinit var binding: ArFragmentBinding

    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View
    private lateinit var actionButton: ExtendedFloatingActionButton

    private lateinit var cursorNode: CursorNode
    private lateinit var modelNode: ArModelNode
    private lateinit var viewNode: ViewNode
    private lateinit var prescNoteViewNode: ViewNode
    private lateinit var prescNotePhotoNode: ViewNode


    private var presc_label: String? = null
    private var presc_time: String? = null
    private var presc_notes_instructions: String? = null
    private var notes_enabled :Boolean = false
    private var presc_photo: String? = null

    private var drug: DrugModel? = null

//    private lateinit var yuvConverter: YuvToRgbConverter
//    private var isDetected: Boolean = false

    private var isAnchored: Boolean = true

    private val menuItems = arrayOf(
//        "Brand Name",
        "Generic Name",
        "Administration",
        "Dosage",
        "Drug Interactions",
        "Indication",
        "Precaution",
        "Storage"
    )
    private var initiallyLoaded = true
    private val content: ArrayList<CharSequence>? = arrayListOf<CharSequence>()
    private var contentPageNum: Int = 0
    private var startTime = SystemClock.elapsedRealtime()

//    private var viewRenderable: ViewRenderable? = null

    private var drug_name: String? = null
    private var drug_file_name: String = "forxiga"

    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
            actionButton.isGone = value
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            drug_name = requireArguments().getString("predictedDrug")
            if (drug_name != null) {
                drug_file_name = drug_name as String
                while (drug_file_name.contains(" ")) {
                    drug_file_name = drug_file_name.replace(" ", "_")
                }
            }

            notes_enabled = requireArguments().getBoolean("notes_enabled", false)

            if (notes_enabled) {
                presc_label = requireArguments().getString("presc_label")
                presc_time = requireArguments().getString("presc_time")
                presc_notes_instructions = requireArguments().getString("presc_notes_instructions")
                presc_photo = requireArguments().getString("presc_photo")
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ArFragmentBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            db = FirebaseFirestore.getInstance()

            sceneView = binding.sceneView

            loadingView = view.findViewById(R.id.loadingView)
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.prescribing_list_item,
                R.id.infoItem,
                menuItems
            )
            val dropdown = binding.menu.editText as? AutoCompleteTextView
            dropdown?.setText(menuItems[1], false)

            actionButton =
                view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
                    val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                    doOnApplyWindowInsets { systemBarsInsets ->
                        (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                            systemBarsInsets.bottom + bottomMargin
                    }
                    setOnClickListener {
                        Constants.startTime = SystemClock.elapsedRealtime()
                        actionButtonClicked()
                        dropdown?.setAdapter(adapter)

                    }
                }
            sceneView.lightEstimationMode = LightEstimationMode.DISABLED

            val root = ViewRenderableInfosBinding.inflate(layoutInflater)

            initDrugPrescriptionView(root)
            loadDrugData(drug_name!!)
            if (drug != null) {
                root.drugName.text = Html.fromHtml(
                    drug!!.brand_name,
                    Html.FROM_HTML_MODE_COMPACT
                )
                if (initiallyLoaded) {
                    root.Content.text = Html.fromHtml(
                        drug!!.administration,
                        Html.FROM_HTML_MODE_COMPACT
                    )
                }
                onSelectedPrescribingInfo(root, dropdown)
            }
            initDrugModel()

            cursorNode = CursorNode(
                context = requireContext(),
                lifecycle = lifecycle
            ).apply {
                onTrackingChanged = { _, _, _ ->
                }
            }
            sceneView.addChild(cursorNode)

            if (notes_enabled) {
                Log.e("Error Notes", "Error in notes found !")
                val notesRoot = ViewRenderableNoteInfosBinding.inflate(layoutInflater)
                val photoRoot = ViewRenderablePhotoBinding.inflate(layoutInflater)

                val content = getString(
                    R.string.presc_notes_render_content,
                    presc_label, presc_time, presc_notes_instructions
                )
                notesRoot.prescNotesContent.text = Html.fromHtml(
                    content, Html.FROM_HTML_MODE_LEGACY
                )

                if (presc_photo != null) {
                    val file = File(presc_photo)
                    val imageUri = Uri.fromFile(file)
                    Glide.with(this@ARFragment)
                        .load(imageUri)
                        .override(1080, 1920)
                        .into(photoRoot.renderablePrescriptionPhoto);
                }

                initPrescriptionNotesView(notesRoot)

                initPrescriptionPhotoView(photoRoot)

            }

            binding.prevButton.setOnClickListener {

                if (contentPageNum > 0) {
                    contentPageNum -= 1
                    addEllipsisInTruncatedText(root)
                }
            }

            binding.nextButton.setOnClickListener {
                if (contentPageNum < content?.size!! - 1) {
                    contentPageNum += 1
                    addEllipsisInTruncatedText(root)
                }
            }
        }
    }

    private fun initDrugPrescriptionView( root: ViewRenderableInfosBinding) {
        viewNode = ViewNode(
            position = Position(0.0f, 1f, 0.0f),
            scale = Scale(0.7f)
        )

        viewNode.loadCustomView(
            context = requireContext(),
            lifecycle = lifecycle,
            layout = root,
        )
    }

    private fun initDrugModel() {
        modelNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL)
        modelNode.loadModelAsync(context = requireContext(),
            lifecycle = lifecycle,
            glbFileLocation = "models/${drug_file_name}_final.glb",
            autoAnimate = true,
            autoScale = false,
            centerOrigin = Position(z = 1.0f),

            onLoaded = {_->
                isAnchored = true
                actionButton.isGone = true
                isLoading = false
            },

        )
        modelNode.apply {
            onTrackingChanged = { _, isTracking, _ ->
                actionButton.isGone = !isTracking
            }

        }
    }

    private fun initPrescriptionNotesView(notesRoot: ViewRenderableNoteInfosBinding) {
        prescNoteViewNode = ViewNode(
            position = Position(0.0f, 1f, 0.0f),
            scale = Scale(0.7f)
        )

        prescNoteViewNode.loadCustomView(
            context = requireContext(),
            lifecycle = lifecycle,
            layout = notesRoot,
        )
    }

    private fun initPrescriptionPhotoView(photoRoot: ViewRenderablePhotoBinding) {
        prescNotePhotoNode = ViewNode(
            position = Position(0.0f, 1f, 0.0f),
            scale = Scale(0.7f)
        )

        prescNotePhotoNode.loadCustomView(
            context = requireContext(),
            lifecycle = lifecycle,
            layout = photoRoot,
        )
    }

    private fun addEllipsisInTruncatedText (root: ViewRenderableInfosBinding) {
        if (content != null) {
            if (content.size > 1) {
                if (contentPageNum < content.size - 1) {
                    root.Content.text = content[contentPageNum] + "..."
                    return
                }
                root.Content.text = content[contentPageNum]
            }
        }
    }

    private fun onSelectedPrescribingInfo(
        root: ViewRenderableInfosBinding,
        dropdown: AutoCompleteTextView?
    ) {

        dropdown?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ -> //                Toast.makeText(requireContext(), "Position: ${position}", Toast.LENGTH_SHORT).show()
                initiallyLoaded = false
                contentPageNum = 0
                Constants.startTime = SystemClock.elapsedRealtime()
                if (content!!.size!! > 0) {
                    content.clear()
                }

                root.drugName.setTextColor(Color.BLACK)
                root.Content.setTextColor(Color.BLACK)
                var description = ""
                if (position == 0) { // if choice is Generic Name
                    description = drug!!.generic_name
                    //                    root.Content.text = Html.fromHtml(drug.generic_name.toString(), Html.FROM_HTML_MODE_COMPACT)
                } else if (position == 1) { // if choice is Administration
                    description = drug!!.administration
                    //                    root.Content.text = Html.fromHtml(drug.administration.toString(), Html.FROM_HTML_MODE_COMPACT)
                } else if (position == 2) { // if choice is Dosage
                    description = drug!!.dosage
                    //                    root.Content.text = Html.fromHtml(drug.dosage.toString(), Html.FROM_HTML_MODE_COMPACT)
                } else if (position == 3) { // if choice is Drug Interaction
                    description = drug!!.drug_interactions
                    //                    root.Content.text = Html.fromHtml(drug.drug_interactions.toString(), Html.FROM_HTML_MODE_COMPACT)
                } else if (position == 4) { // if choice is Indication
                    description = drug!!.indication
                    //                    root.Content.text = Html.fromHtml(drug.indication.toString(), Html.FROM_HTML_MODE_COMPACT)
                } else if (position == 5) { // if choice is Precaution
                    description = drug!!.precaution
                    //                    root.Content.text = Html.fromHtml(drug.precaution.toString(), Html.FROM_HTML_MODE_COMPACT)
                } else if (position == 6) { // if choice is Storage
                    description = drug!!.storage
                    //                    root.Content.text = Html.fromHtml(drug.storage.toString(), Html.FROM_HTML_MODE_COMPACT)
                }

                setContentTextView(root, description)
                Log.d("Time elapsed: ", "Time elapsed in changing Prescription Info : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")

            }

    }

    private fun setContentTextView (root: ViewRenderableInfosBinding, description: String) {
        var formattedDescription: CharSequence = Html.fromHtml(description , Html.FROM_HTML_MODE_COMPACT)
        if (formattedDescription.length <= 1100) {
            binding.prevButton.visibility = View.GONE
            binding.nextButton.visibility = View.GONE
            root.Content.text = formattedDescription
//                    root.Content.text = Html.fromHtml(description.substring(0,1200) + "..." , Html.FROM_HTML_MODE_COMPACT)
        } else {
            while (formattedDescription.length > 0) {
                if (formattedDescription.length> 1100) {
                    content?.add(formattedDescription.subSequence(0, 1100))
                    formattedDescription = formattedDescription.removeRange(
                        0..1099)
                } else {
                    content?.add(formattedDescription.subSequence(
                        0, formattedDescription.length))
                    formattedDescription = formattedDescription.removeRange(
                        0..formattedDescription.length-1)
                }
            }
            root.Content.text = content!![contentPageNum] + "..."
            binding.prevButton.visibility = View.VISIBLE
            binding.nextButton.visibility = View.VISIBLE
        }
    }

    private fun anchorOrMove(/*anchor: Anchor?*/) {

            if (!modelNode.children.contains(viewNode)) {
                modelNode.addChild(viewNode).apply {
                    viewNode.position = Position(x = 1.20f, y = -1.0f, z = 0.0f)
                    viewNode.rotation =  Rotation(y = -35.0f)//Quaternion(y = -10.0f).toEulerAngles()// sceneView.camera.rotation
                    Toast.makeText( context, viewNode.rotation.toString(),Toast.LENGTH_LONG).show()
                }
            }

            if (notes_enabled) {
                if (!modelNode.children.contains(prescNoteViewNode)){
                    modelNode.addChild(prescNoteViewNode).apply {
                        prescNoteViewNode.position = Position(x = -1.20f, y = -1.0f, z = 0.0f)
                        prescNoteViewNode.rotation =  Rotation(y = 35.0f)//Quaternion(y = -10.0f).toEulerAngles()// sceneView.camera.rotation
                        Toast.makeText( context, "Prescription Notes is attached",Toast.LENGTH_LONG).show()
                    }
                }
                if (presc_photo != null && !prescNoteViewNode.children.contains(prescNotePhotoNode)){
                    prescNoteViewNode.addChild(prescNotePhotoNode).apply {
                        prescNotePhotoNode.position = Position(x = -1.5f, y = 1.0f, z = 0.0f)
//                        prescNotePhotoNode.rotation =  Rotation(y = 40.0f)//Quaternion(y = -10.0f).toEulerAngles()//
                        Toast.makeText( context, "Photo is Attached",Toast.LENGTH_LONG).show()
                    }
                }
            }

    }

    private fun actionButtonClicked() {
        if (!modelNode.isAnchored) {
            binding.menu.visibility = View.VISIBLE
            actionButton.text = getString(R.string.move_object)
            actionButton.setIconResource(R.drawable.ic_target)
            sceneView.planeRenderer.isVisible = false

            Toast.makeText(context, "Object is anchored", Toast.LENGTH_SHORT).show()
            if (sceneView.children.contains(cursorNode)){
                val cursor_anchor = cursorNode.createAnchor()

                sceneView.addChild(modelNode)
                modelNode.anchor = cursor_anchor
                anchorOrMove()
                Log.d("Time elapsed: ", "Time elapsed in placing model : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
                sceneView.removeChild(cursorNode)
                cursorNode.detachAnchor()
                cursorNode.parent = null
                return
            }
            modelNode.anchor()
            Log.d("Time elapsed: ", "Time elapsed in placing model : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")

        } else {
            Toast.makeText(context, "Object disanchored", Toast.LENGTH_SHORT).show()

            binding.menu.visibility = View.GONE
            modelNode.anchor = null
            actionButton.text = getString(R.string.place_object)
            actionButton.setIconResource(R.drawable.ic_anchor)
            sceneView.planeRenderer.isVisible = true
            Log.d("Time elapsed: ", "Time elapsed in removing model : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")

        }
    }


    private suspend fun getDataFromFirestore(brand_name: String): DocumentSnapshot{

        val ref = db.collection("drugs")
        val snapshot = ref.whereEqualTo("brand_name_query", brand_name)
            .get()
            .await()
        return snapshot.documents.first()
    }

    private suspend fun  loadDrugData(brand_name: String){

        try {
            val drugData = getDataFromFirestore(brand_name)
            drug = drugData.toObject(DrugModel::class.java)!!

        } catch (e: Exception) {
            Log.d("Error loading Data", e.toString()) //Don't ignore potential errors!
        }

    }

}

private operator fun CharSequence.plus(charSequence: CharSequence): Spannable {
    return SpannableStringBuilder(this).append(charSequence)
}

private fun ViewNode.loadCustomView(
    context: Context,
    lifecycle: Lifecycle? = null,
    layout: ViewBinding,
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
