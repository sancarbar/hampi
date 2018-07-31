package co.sancarbar.hampi.ui.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import co.sancarbar.hampi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_entry.*
import kotlinx.android.synthetic.main.new_entry_form.*
import model.Plant
import ui.utils.DialogFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Santiago Carrillo
 * 7/24/18.
 */

const val TAKE_PHOTO_REQUEST_CODE = 85

const val SELECT_IMAGE_REQUEST_CODE = 86

const val CREATED_ENTRY_KEY = "created_entry_key"

const val DB_COLLECTION_NAME = "plants"

class NewEntryActivity : AppCompatActivity() {


    private var currentPhotoUri: Uri? = null

    private var storage: FirebaseStorage = FirebaseStorage.getInstance("gs://hampi-m.appspot.com")

    private var storageRef = storage.reference

    private lateinit var currentEntry: Plant

    private var firebaseAuth = FirebaseAuth.getInstance()

    var firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_entry)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.Create_Entry)


        initButtonsBindings()

    }

    private fun initButtonsBindings() {
        val listener = DialogInterface.OnClickListener { dialog, which ->

            when (which) {
                0 -> startPhotoSelectingFromExistingSources()
                1 -> dispatchTakePictureIntent()
            }
            dialog.dismiss()
        }

        imageView.setOnClickListener {
            DialogFactory.showSingleChoiceDialog(this, R.array.add_photo_options_array,
                    R.string.Select_one_option, listener)
        }

        save.setOnClickListener {
            if (validateForm()) {
                save.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                uploadImage()
            }
        }
    }

    private fun saveEntryToDatabase(imageUrl: String) {
        val user = firebaseAuth.currentUser!!.email
        currentEntry = Plant(name.text.toString(), description.text.toString(), imageUrl, user!!)
        firestore.collection(DB_COLLECTION_NAME).add(currentEntry).addOnFailureListener {
            progressBar.visibility = View.GONE
            save.visibility = View.VISIBLE
            DialogFactory.showInfoDialog(this, R.string.Upload_entry_error, R.string.Unable_to_upload_your_entry_Please_try_again)
        }.addOnCompleteListener {
            val data = Intent()
            data.putExtra(CREATED_ENTRY_KEY, currentEntry)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun validateForm(): Boolean {
        if (name.text.isEmpty()) {
            name.error = getString(R.string.please_fill_the_plant_name)
            return false
        } else {
            name.error = null
        }
        if (description.text.isEmpty()) {
            description.error = getString(R.string.please_fill_the_plant_name)
            return false
        } else {
            description.error = null
        }
        if (currentPhotoUri == null) {

            DialogFactory.showInfoDialog(this, R.string.Missing_photo, R.string.Please_add_a_photo)
            return false
        }
        return true
    }

    private fun startPhotoSelectingFromExistingSources() {

        val pictureIntent = Intent()
        if (Build.VERSION.SDK_INT < 19) {
            pictureIntent.action = Intent.ACTION_GET_CONTENT
        } else {
            pictureIntent.action = Intent.ACTION_OPEN_DOCUMENT
            pictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        }
        pictureIntent.type = "image/*"
        val intent = Intent.createChooser(pictureIntent, getString(R.string.Choose_from_library))
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File?
            // Create the File where the photo should go
            try {

                photoFile = createImageFile()
                val photoURI: Uri = FileProvider.getUriForFile(this, "co.sancarbar.hampi.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE)
            } catch (ex: IOException) {
                // Error occurred while creating the File

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                TAKE_PHOTO_REQUEST_CODE -> updatePictureFromPhotoFile()

                SELECT_IMAGE_REQUEST_CODE -> {
                    imageView.setImageURI(data!!.data)
                    currentPhotoUri = data.data
                }
            }

        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updatePictureFromPhotoFile() {
        imageView.visibility = View.VISIBLE
        // Get the dimensions of the View
        val targetW = imageView.width
        val targetH = imageView.height

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoUri.toString(), bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            bmOptions.inPurgeable = true
        }
        val bitmap = BitmapFactory.decodeFile(currentPhotoUri!!.path, bmOptions)
        imageView.setImageBitmap(bitmap)
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        currentPhotoUri = Uri.fromFile(image)
        return image
    }

    private fun uploadImage() {

        if (currentPhotoUri != null) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val photoRef = storageRef.child("/plants-images/$timeStamp")
            val uploadTask = photoRef.putFile(currentPhotoUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    progressBar.visibility = View.GONE
                    save.visibility = View.VISIBLE
                    throw task.exception!!
                }
                photoRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    saveEntryToDatabase(downloadUri.toString())
                } else {
                    progressBar.visibility = View.GONE
                    save.visibility = View.VISIBLE
                    DialogFactory.showInfoDialog(this, R.string.Upload_photo_error, R.string.Unable_to_upload_your_photo_Please_try_again)
                }
            }
        }
    }
}