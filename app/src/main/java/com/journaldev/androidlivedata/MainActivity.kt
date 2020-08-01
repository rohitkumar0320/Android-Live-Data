package com.journaldev.androidlivedata

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.journaldev.androidlivedata.MainActivity.FavAdapter.FavViewHolder
import kotlinx.android.synthetic.main.list_item_row.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mFavAdapter: FavAdapter? = null
    private var mFavViewModel: FavouritesViewModel? = null
    private var mFav: List<Favourites>? = null

    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null

    //val fab: FloatingActionButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        mFavViewModel = ViewModelProviders.of(this).get(FavouritesViewModel::class.java)

        val favsObserver: Observer<List<Favourites>?> = Observer { updatedList ->
            if (mFav == null) {
                mFav = updatedList
                mFavAdapter = FavAdapter()
                recyclerView.adapter = mFavAdapter
            } else {
                val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return mFav!!.size
                    }

                    override fun getNewListSize(): Int {
                        return updatedList!!.size
                    }

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return mFav!![oldItemPosition].mId ==
                                updatedList!![newItemPosition].mId
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val oldFav = mFav!![oldItemPosition]
                        val newFav = updatedList!![newItemPosition]
                        return oldFav == newFav
                    }
                })
                result.dispatchUpdatesTo(mFavAdapter!!)
                Collections.reverse(updatedList)
                mFav = updatedList
            }
        }
        fab.setOnClickListener(View.OnClickListener {

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dailog)
            dialog.setTitle("New User")/**/
            val nameTxt = dialog.findViewById<EditText>(R.id.name)
            val emailTxt = dialog.findViewById<EditText>(R.id.email)
            val phoneTxt = dialog.findViewById<EditText>(R.id.phoneNumber)
            val date = Date().time
            val photoAdd = dialog.findViewById<Button>(R.id.photoButton)

            photoAdd.setOnClickListener {
                val dialog = Dialog(this@MainActivity)
                dialog.setContentView(R.layout.options)
                dialog.setTitle("Select option")
                val gallery = dialog.findViewById<Button>(R.id.gallery)
                gallery.setOnClickListener(View.OnClickListener {
                    openGallery()
                    dialog.dismiss()
                })

                val camera = dialog.findViewById<Button>(R.id.camera)
                camera.setOnClickListener(View.OnClickListener {
                    openCamera()
                    dialog.dismiss()
                })
                dialog.show()
                //openGallery()
                // Toast.makeText(this@MainActivity,"upload Successful",Toast.LENGTH_SHORT)
            }

            val adButton = dialog.findViewById<Button>(R.id.addButton)
            adButton.setOnClickListener(View.OnClickListener {
                val newName = nameTxt.text.toString()
                val nameRegex = "^[A-Za-z]+$".toRegex()
                if (newName.isEmpty()) {
                    nameTxt.setError("Cannot be empty")
                    nameTxt.requestFocus()
                    return@OnClickListener
                } else if (!(newName).matches(nameRegex)) {
                    nameTxt.setError("Special character not allowed")
                    nameTxt.requestFocus()
                    return@OnClickListener
                }

                val emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".toRegex()

                val newEmail = emailTxt.text.toString()
                if (newEmail.isEmpty()) {
                    emailTxt.setError("Cannot be empty")
                    emailTxt.requestFocus()
                    return@OnClickListener
                } else if (!(newEmail).matches(emailRegex)) {
                    emailTxt.setError("Invalid Email")
                    emailTxt.requestFocus()
                    return@OnClickListener

                }
                val newNum = phoneTxt.text.toString()

                val phoneRegex = "^[0-9]{10}\$".toRegex()

                if (!(newNum).matches(phoneRegex)) {
                    phoneTxt.requestFocus()
                    phoneTxt.setError("Enter correct Number")
                    return@OnClickListener
                }
                val imageUri: String = image_uri.toString()
                mFavViewModel!!.addFav(imageUri, newName, date, newEmail, newNum)
                dialog.dismiss()
            })

            val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
            cancelButton.setOnClickListener(View.OnClickListener {
                dialog.dismiss()
            })
            dialog.show()
        })
        mFavViewModel!!.favs.observe(this, favsObserver)
    }


    inner class FavAdapter : RecyclerView.Adapter<FavViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_row, parent, false)
            return FavViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
            val favourites = mFav!![position]
            holder.mTxtUrl.text = favourites.mUrl
            holder.mTxtEmail.text = favourites.mEmail
            holder.mTxtPhoneNumber.text = favourites.mPhoneNumber
            //holder.mTxtDate.setText((new Date(favourites.mDate).toString()));
        }

        override fun getItemCount(): Int {
            return mFav!!.size
        }

        inner class FavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var mImageView: ImageView
            var mTxtUrl: TextView
            var mTxtEmail: TextView
            var mTxtPhoneNumber: TextView
            var mTxtDate: TextView

            init {
                mImageView = itemView.findViewById(R.id.photo)
                mTxtUrl = itemView.findViewById(R.id.tvUrl)
                mTxtDate = itemView.findViewById(R.id.tvDate)
                mTxtEmail = itemView.findViewById(R.id.tvEmail)
                mTxtPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber)
                val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)
                val photoButton = itemView.findViewById<ImageView>(R.id.photo)
                val btnUpdate = itemView.findViewById<ImageButton>(R.id.btnEdit)

                photoButton.setOnClickListener(View.OnClickListener {
                    val dialogNew = Dialog(this@MainActivity)
                    dialogNew.setContentView(R.layout.options)
                    dialogNew.setTitle("Select option")
                    val gallery = dialogNew.findViewById<Button>(R.id.gallery)
                    gallery.setOnClickListener(View.OnClickListener {
                        openGallery()
                        dialogNew.dismiss()
                    })

                    val camera = dialogNew.findViewById<Button>(R.id.camera)
                    camera.setOnClickListener(View.OnClickListener {
                        openCamera()
                        dialogNew.dismiss()
                    })
                    dialogNew.show()

                })
                btnDelete.setOnClickListener {
                    val pos = adapterPosition
                    val favourites = mFav!![pos]
                    mFavViewModel!!.removeFav(favourites.mId)
                }

                btnUpdate.setOnClickListener(View.OnClickListener {
                    val dialog = Dialog(this@MainActivity)
                    dialog.setContentView(R.layout.dailog)
                    dialog.setTitle("New User")
                    val nameTxt = dialog.findViewById<EditText>(R.id.name)
                    val newName = nameTxt.text.toString()
                    val emailTxt = dialog.findViewById<EditText>(R.id.email)
                    val phoneTxt = dialog.findViewById<EditText>(R.id.phoneNumber)
                    val adButton = dialog.findViewById<Button>(R.id.addButton)
                    val photoAdd = dialog.findViewById<Button>(R.id.photoButton)

                    photoAdd.setOnClickListener(View.OnClickListener {
                        openGallery()
                        Toast.makeText(this@MainActivity, "upload Successful", Toast.LENGTH_SHORT)
                    })

                    adButton.setOnClickListener(View.OnClickListener {
                        val pos = adapterPosition
                        val favourites = mFav!![pos]

                        if (newName.isEmpty()) {
                            nameTxt.setError("cannot be empty")
                            nameTxt.requestFocus()
                        }

                        val newName = nameTxt.text.toString()
                        val nameRegex = "^[A-Za-z]+$".toRegex()
                        if (newName.isEmpty()) {
                            nameTxt.setError("cannot be empty")
                            nameTxt.requestFocus()
                            return@OnClickListener
                        } else if (!(newName).matches(nameRegex)) {
                            nameTxt.setError("Special character not allowed")
                            nameTxt.requestFocus()
                            return@OnClickListener
                        }

                        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+$".toRegex()

                        val newEmail = emailTxt.text.toString()
                        if (newEmail.isEmpty()) {
                            emailTxt.setError("cannot be empty")
                            emailTxt.requestFocus()
                            return@OnClickListener
                        } else if (!(newEmail).matches(emailRegex)) {
                            emailTxt.setError("Incorrect Email")
                            emailTxt.requestFocus()
                            return@OnClickListener

                        }
                        val newNum = phoneTxt.text.toString()

                        val phoneRegex = "^[0-9]{10}\$".toRegex()

                        if (!(newNum).matches(phoneRegex)) {
                            phoneTxt.requestFocus();
                            phoneTxt.setError("Enter correct Number")
                            return@OnClickListener
                        }

                        val imageUri: String = image_uri.toString()

                        val isUpdate = mFavViewModel!!.updateFav(imageUri, nameTxt.text.toString(), Date().time, emailTxt.text.toString(), phoneTxt.text.toString(), favourites.mId)
                        if (isUpdate) {
                            mFav!![pos].mImage = imageUri
                            mFav!![pos].mUrl = nameTxt.text.toString()
                            mFav!![pos].mEmail = emailTxt.text.toString()
                            mFav!![pos].mPhoneNumber = phoneTxt.text.toString()
                            mFav!![pos].mDate = Date().time
                            notifyDataSetChanged()
                            dialog.dismiss()
                        }
                    })
                    val cancel = dialog.findViewById<Button>(R.id.cancelButton)
                    cancel.setOnClickListener(View.OnClickListener {
                        dialog.dismiss()
                    })
                    dialog.show()
                })
            }
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        // photo.setImageURI(image_uri)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private var IMAGE_PICK_CODE = 1000
        //Permission code
        // private val PERMISSION_CODE = 1001;
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK && requestCode === IMAGE_PICK_CODE) {
            try {
                val imgUri = data?.data
                val imageStream = contentResolver.openInputStream(imgUri)
                // val picture=getEncodedString(imageStream)
                photo.setImageBitmap(BitmapFactory.decodeStream(imageStream))
            } catch (exception: IOException) {
                exception.stackTrace
            }
        } else if (resultCode === Activity.RESULT_OK && requestCode === IMAGE_CAPTURE_CODE) {
            val extras: Bundle? = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap
            photo.setImageBitmap(imageBitmap)
            // photo.setImageURI(image_uri)
        }
    }
}
