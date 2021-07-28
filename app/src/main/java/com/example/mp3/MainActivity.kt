package com.example.mp3

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.songs_ticket.view.*

class MainActivity : AppCompatActivity() {
    var listSongs = ArrayList<SongInfo>()
    var adapter:MySongAdapter?=null

    var mp:MediaPlayer?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LoadURLOnline()
        CheckUserPermsions()
        var mytrack = mySongTrack()
        mytrack.start()
    }
    fun LoadURLOnline(){
        listSongs.add(SongInfo("s1","a1","url1"))
        listSongs.add(SongInfo("s2","a2","url2"))
        listSongs.add(SongInfo("s3","a3","url3"))
        listSongs.add(SongInfo("s4","a4","url4"))
    }
    inner class MySongAdapter:BaseAdapter {
        var myListSong = ArrayList<SongInfo>()
        constructor(myListSong:ArrayList<SongInfo>):super(){
          this.myListSong = myListSong
        }
        override fun getCount(): Int {
        return this.myListSong.size
        }

        override fun getItem(position: Int): Any {
            return this.myListSong[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val myView = layoutInflater.inflate(R.layout.songs_ticket,null)
            val Song = this.myListSong[position]
            myView.tvSongName.setText(Song.Title)
            myView.tvAuthor.setText(Song.AuthorName)
            myView.buPlay.setOnClickListener{
                if(mp!=null)
                {
                   mp!!.stop()
                }
                mp = null
                mp = MediaPlayer()
                if(myView.buPlay.text.equals("STOP")){
                    sbProgress.progress = 0
                    myView.buPlay.text = "PLAY"
                }
                else {
                        try {
                            mp!!.setDataSource(Song.SongURL)
                            mp!!.prepare()
                            mp!!.start()
                            myView.buPlay.text = "STOP"
                            sbProgress.max = mp!!.duration
                        } catch (ex: Exception) {
                        }
                    }
                }

            return myView
        }

    }
    inner class mySongTrack():Thread() {
        override fun run() {
            while (true) {
                try {
                    Thread.sleep(1000)

                } catch (ex: Exception) {
                }
                runOnUiThread {
                    if (mp != null) {
                        sbProgress.progress = mp!!.currentPosition
                        if (mp!!.currentPosition == mp!!.duration) {
                            sbProgress.progress = 0

                        }

                    }
                }

            }
        }
    }

    private fun CheckUserPermsions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_ASK_PERMISSIONS)
                return
            }
        }
        LoadSong()

    }
    private val REQUEST_CODE_ASK_PERMISSIONS = 123


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LoadSong()
            } else {
                // Permission Denied
                Toast.makeText(this, "Can't access media", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    fun LoadSong(){
        var allSongsURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var selection = MediaStore.Audio.Media.IS_MUSIC+"!=0"
        var cursor = contentResolver.query(allSongsURI,null,selection,null,null)
        if(cursor!=null)
        {
            if(cursor!!.moveToFirst()){
                do {
                    var songURL =cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DATA))
                    var authorName =cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    var songName =cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    listSongs.add(SongInfo(songName,authorName,songURL))


                }while(cursor!!.moveToNext())
            }
            cursor!!.close()
            adapter = MySongAdapter(listSongs)
            lsListSongs.adapter = adapter

        }
    }


}