package co.sancarbar.hampi.ui.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import co.sancarbar.hampi.R
import kotlinx.android.synthetic.main.activity_new_entry.*
import kotlinx.android.synthetic.main.new_entry_form.*
import ui.utils.DialogFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Santiago Carrillo
 * 7/24/18.
 */

val REQUEST_TAKE_PHOTO = 1

class NewEntryActivity : AppCompatActivity() {


    lateinit var currentPhotoPath: String

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
                0 -> openGallery()
                1 -> dispatchTakePictureIntent()
            }
            dialog.dismiss()
        }

        imageView.setOnClickListener {
            DialogFactory.showSingleChoiceDialog(this, R.array.add_photo_options_array,
                    R.string.Select_one_option, listener)
        }
    }

    private fun openGallery() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            } catch (ex: IOException) {
                // Error occurred while creating the File

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
//            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
//            imageView.setImageBitmap(bitmap)
//            imageView.visibility = View.VISIBLE
            setPic()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setPic() {
        imageView.visibility = View.VISIBLE
        // Get the dimensions of the View
        val targetW = imageView.width
        val targetH = imageView.height

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
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
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
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

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }
}