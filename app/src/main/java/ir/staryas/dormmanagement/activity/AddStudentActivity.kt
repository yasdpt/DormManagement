package ir.staryas.dormmanagement.activity

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import id.zelory.compressor.Compressor
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.Msg
import ir.staryas.dormmanagement.model.StudentObj
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.PrefManage
import ir.staryas.dormmanagement.util.ViewDialog
import ir.staryas.dormmanagement.util.getJsonExtra
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.activity_add_student.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.Exception

class AddStudentActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CHOOSE_1 = 111

        private var auxFile1:String? = "null"
        private const val PERMISSION_REQUEST_CODE: Int = 101
        private lateinit var name:String
        private lateinit var family:String
        private lateinit var image:String
        private lateinit var pid:String
        private lateinit var natId:String
        private var sDebt: Int = 0
        private var sCredit: Int = 0
        private var sId: Int = 0
        private lateinit var mode: String
        private lateinit var phone:String
    }

    private lateinit var viewDialog: ViewDialog

    private lateinit var pickImageDialog:Dialog

    private val apiClient = ApiClient()
    private lateinit var prefManage: PrefManage


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        initToolbar()

        initComponent()


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.asToolbar)
        ViewCompat.setLayoutDirection(toolbar, ViewCompat.LAYOUT_DIRECTION_RTL)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = "ثبت دانشجو"
        supportActionBar!!.elevation = 0.0f
        AdminActivity.tools.setSystemBarColor(this,
            R.color.colorPrimaryDark
        )
        AdminActivity.tools.setSystemBarLight(this)
    }

    private fun initComponent(){
        viewDialog = ViewDialog(this)
        prefManage = PrefManage(this)

        val extras = intent.extras
        if(extras != null){
            mode = extras.getString("mode")!!
            when (mode) {
                "add" -> {

                }
                "edit" -> {
                    val student = intent.getJsonExtra("studentObj", StudentObj::class.java)
                    val stdName = student!!.studentFullName.split(' ')
                    etStudentName.setText(stdName[0])
                    etStudentFamily.setText(stdName[1])
                    etStudentNatId.setText(student.studentNatId)
                    etStudentPId.setText(student.studentPId)
                    etStudentPhone.setText(student.studentPhone)
                    sDebt = student.studentDebt.toInt()
                    sCredit = student.studentCredit.toInt()
                    sId = student.studentId.toInt()
                    if (student.studentImage != "null"){
                        Glide.with(this)
                            .asBitmap()
                            .load("http://staryas.ir/dorm/api/images/${student.studentImage}")
                            .into(object : CustomTarget<Bitmap>(){
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    displayImageEdit(resource)
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // this is called when imageView is cleared on lifecycle call or for
                                    // some other reason.
                                    // if you are referencing the bitmap somewhere else too other than this imageView
                                    // clear it here as you can no longer have the bitmap
                                }
                            })

                    }
                    title = "ویرایش دانشجو"
                    btnAddStudent.text = "ویرایش دانشجو"
                }
            }
        }

        btnStudentImage.setOnClickListener {
            if (auxFile1 == "null"){
                if (checkPermission()) openAlbum() else requestPermission()
            } else {
                showPickImageDialogWithDelete()
            }
        }



        btnAddStudent.setOnClickListener {
            try {

                if (isNetworkAvailable()){

                    if (mode == "add") {
                        image = if (auxFile1 != "null"){
                            auxFile1!!
                        } else {
                            "null"
                        }

                        name = etStudentName.text.toString().trim()
                        family = etStudentFamily.text.toString().trim()
                        pid = etStudentPId.text.toString().trim()
                        natId = etStudentNatId.text.toString().trim()
                        phone = etStudentPhone.text.toString().trim()
                        sDebt = prefManage.getTuition()!!


                        if (validate(name, family, pid, natId, phone)){
                            viewDialog.showDialog()
                            addStudent(name, family, image, pid, natId, phone, sDebt,0)
                        }
                    } else if (mode == "edit"){
                        image = if (auxFile1 != "null"){
                            auxFile1!!
                        } else {
                            "null"
                        }

                        name = etStudentName.text.toString().trim()
                        family = etStudentFamily.text.toString().trim()
                        pid = etStudentPId.text.toString().trim()
                        natId = etStudentNatId.text.toString().trim()
                        phone = etStudentPhone.text.toString().trim()



                        if (validate(name, family, pid, natId, phone)){
                            viewDialog.showDialog()
                            editStudent(sId,name, family, image, pid, natId, phone, sDebt, sCredit)
                        }
                    }



                } else {
                    toast(getString(R.string.no_internet), this)
                }
            } catch (e:Exception){

            }
        }


    }

    private fun validate(studentName: String,studentFamily: String,studentPId:String, studentNatId:String, studentPhone:String):Boolean {
        var valid = true

        if (studentName.isEmpty() || studentName.length < 3 )
        {
            tilStudentName.error = "نام باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilStudentName.error = null
        }

        if (studentFamily.isEmpty() || studentFamily.length < 3)
        {
            tilStudentFamily.error = "نام خانوادگی باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilStudentFamily.error = null
        }

        if (studentPId.isEmpty() || studentPId.length < 3)
        {
            tilStudentFamily.error = "شماره دانشجویی باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilStudentFamily.error = null
        }

        if (studentNatId.isEmpty() || studentNatId.length < 3)
        {
            tilStudentFamily.error = "کد ملی باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilStudentFamily.error = null
        }

        if (studentPhone.isEmpty() || studentPhone.length < 3)
        {
            tilStudentFamily.error = "شماره موبایل باید حداقل ۳ حرف باشد"
            valid = false
        } else {
            tilStudentFamily.error = null
        }

        return valid
    }

    private fun addStudent(sName:String, sFamily:String, sImage:String, sPId:String, sNatId:String, sPhone: String, sDebt: Int, sCredit:Int){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.manageStudents("add",0,sName, sFamily, sImage, sPId, sNatId, sPhone, sDebt, sCredit)

        call.enqueue(object : Callback<Msg>{
            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                viewDialog.hideDialog()
                if (response.body()!!.success == 1){
                    etStudentName.setText("")
                    etStudentFamily.setText("")
                    etStudentPId.setText("")
                    etStudentNatId.setText("")
                    etStudentPhone.setText("")
                    btnStudentImage.scaleType = ImageView.ScaleType.CENTER
                    btnStudentImage.setImageResource(R.drawable.personnoimage)
                    toast(response.body()!!.message!!, applicationContext)
                } else {
                    toast(response.body()!!.message!!, applicationContext)
                }
            }

            override fun onFailure(call: Call<Msg>, t: Throwable) {
                viewDialog.hideDialog()
                toast(getString(R.string.network_failure), applicationContext)
            }

        })

    }

    private fun editStudent(studentId:Int,sName:String, sFamily:String, sImage:String, sPId:String, sNatId:String, sPhone: String, sDebt: Int, sCredit:Int){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.manageStudents("edit",
            studentId,sName, sFamily, sImage, sPId, sNatId, sPhone, sDebt, sCredit)

        call.enqueue(object : Callback<Msg>{
            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                viewDialog.hideDialog()
                if (response.body()!!.success == 1){
                    etStudentName.setText("")
                    etStudentFamily.setText("")
                    etStudentPId.setText("")
                    etStudentNatId.setText("")
                    etStudentPhone.setText("")
                    btnStudentImage.scaleType = ImageView.ScaleType.CENTER
                    btnStudentImage.setImageResource(R.drawable.personnoimage)
                    toast(response.body()!!.message!!, applicationContext)
                    finish()
                } else {
                    toast(response.body()!!.message!!, applicationContext)
                }
            }

            override fun onFailure(call: Call<Msg>, t: Throwable) {
                viewDialog.hideDialog()
                toast(getString(R.string.network_failure), applicationContext)
            }

        })
    }

    private fun showPickImageDialogWithDelete(){
        pickImageDialog = Dialog(this)
        pickImageDialog.setContentView(R.layout.sheet_list_delete)
        pickImageDialog.setCancelable(true)

        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(pickImageDialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val btnDeleteImage:LinearLayout = pickImageDialog.findViewById(
            R.id.btnDeleteImage
        )


        btnDeleteImage.setOnClickListener {
            hidePickImageDialogWithDelete()
            auxFile1 = "null"
            btnStudentImage.scaleType = ImageView.ScaleType.CENTER
            btnStudentImage.setImageResource(R.drawable.personnoimage)
        }

        pickImageDialog.show()
        pickImageDialog.window?.attributes = lp

    }

    private fun hidePickImageDialogWithDelete(){
        pickImageDialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_IMAGE_CHOOSE_1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data)
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(applicationContext, CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    openAlbum()

                } else {
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {

            }
        }
    }

    private fun openAlbum(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_CHOOSE_1)
    }

    private fun handleImage(data: Intent?){
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri?.authority){
                val id = docId.split(":")[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            }
            else if ("com.android.providers.downloads.documents" == uri?.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri?.scheme, ignoreCase = true)){
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri?.scheme, ignoreCase = true)){
            imagePath = uri?.path
        }
        displayImage(imagePath)
    }

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = this.contentResolver.query(uri!!, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    private fun displayImage(imagePath: String?){
        if (imagePath != null) {
            val compressedImageFile = Compressor(this).compressToBitmap(File(imagePath))


            val baos = ByteArrayOutputStream()
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG,50,baos)
            val b:ByteArray = baos.toByteArray()



            val requestOption = RequestOptions()
                .centerCrop()
                .transforms(CenterCrop(), RoundedCorners(150))

            when (auxFile1) {
                "null" -> {
                    auxFile1 = Base64.encodeToString(b,Base64.DEFAULT)
                    btnStudentImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this).load(compressedImageFile)
                        .apply(requestOption)
                        .into(btnStudentImage)
                }
            }
        }
        else {
            Toast.makeText(this, "دریافت عکس ناموفق بود", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayImageEdit(image: Bitmap?){
        if (image != null) {
            //val compressedImageFile = Compressor(this).compressToBitmap(image)

            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG,50,baos)
            val b:ByteArray = baos.toByteArray()



            val requestOption = RequestOptions()
                .centerCrop()
                .transforms(CenterCrop(), RoundedCorners(150))

            when (auxFile1) {
                "null" -> {
                    auxFile1 = Base64.encodeToString(b,Base64.DEFAULT)
                    btnStudentImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this).load(image)
                        .apply(requestOption)
                        .into(btnStudentImage)
                }
            }
        }
        else {
            Toast.makeText(this, "دریافت عکس ناموفق بود", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            toast(item.title.toString(), applicationContext)
        }
        return super.onOptionsItemSelected(item)
    }




}
