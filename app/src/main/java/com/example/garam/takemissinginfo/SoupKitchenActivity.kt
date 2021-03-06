package com.example.garam.takemissinginfo

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.garam.takemissinginfo.network.NetworkController
import com.example.garam.takemissinginfo.network.NetworkService
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_soup_kitchen.*
import kotlinx.android.synthetic.main.detail_info_layout.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SoupKitchenActivity : AppCompatActivity(), MapView.MapViewEventListener, MapView.POIItemEventListener {

    private val networkService : NetworkService by lazy{
        NetworkController.instance.networkService
    }

    private lateinit var dialog: Dialog

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        val detailInfo = JsonParser().parse(p1?.userObject.toString()).asJsonObject

        showDialog(detailInfo,p1?.itemName.toString(),
            p1?.mapPoint?.mapPointGeoCoord?.latitude!!,p1.mapPoint.mapPointGeoCoord.longitude)

    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soup_kitchen)

        val mapView = MapView(this)
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.detail_info_layout)

        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            return } else locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val latitude = location.latitude
        val longitude = location.longitude

        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude,longitude),2, true)
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving

        wholeSoupKitchenButton.setOnClickListener {
            mapView.removeAllPOIItems()
            soupKitchenMarker(latitude,longitude,mapView)
        }

        nearBySoupKitchenButton.setOnClickListener {
            mapView.removeAllPOIItems()
            nearByKitchenMarker(latitude,longitude,mapView)
        }

        mapViewLayout.addView(mapView)

    }
    private val failMessage = Toast.makeText(this,"조회에 실패했습니다", Toast.LENGTH_SHORT)

    private fun nearByKitchenMarker(currentLatitude: Double, currentLongitude: Double, mapView: MapView){

        networkService.soupKitchenRequest(currentLatitude, currentLongitude).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                failMessage.show()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when (response.isSuccessful) {
                    true -> {
                        val responseBody = response.body()!!.asJsonObject
                        val data = responseBody["data"].asJsonArray

                        for ( i in 0 until 5){

                            val dataObject = data[i].asJsonObject
                            val facilityName = dataObject["facilityName"].asString
                            val address = dataObject["address"].asString

                            val phoneNumber = if (dataObject["phoneNumber"].isJsonNull) "정보없음"
                            else dataObject["phoneNumber"].asString

                            val operatingTime = if (dataObject["operatingTime"].isJsonNull) "정보없음"
                            else dataObject["operatingTime"].asString

                            val operatingDate = if (dataObject["operatingDate"].isJsonNull) "정보없음"
                            else dataObject["operatingDate"].asString

                            val latitude = dataObject["latitude"].asDouble
                            val longitude = dataObject["longitude"].asDouble

                            val soupKitchenDataObject = dataToJsonObject(facilityName, address, phoneNumber,
                                operatingTime, operatingDate, latitude, longitude)

                            val marker = MapPOIItem()
                            marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude,longitude)
                            marker.itemName = facilityName
                            marker.userObject = soupKitchenDataObject
                            marker.customCalloutBalloon
                            mapView.addPOIItem(marker)
                        }
                    } false -> failMessage.show()
                }
            }
        })
    }

    private fun soupKitchenMarker(currentLatitude: Double, currentLongitude: Double, mapView: MapView){

        networkService.soupKitchenRequest(currentLatitude, currentLongitude).enqueue(object : Callback<JsonObject>{
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                failMessage.show()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when (response.isSuccessful) {
                    true -> {
                        val responseBody = response.body()!!
                        val data = responseBody.get("data").asJsonArray

                        for ( i in 0 until data.size()){

                            val dataObject = data[i].asJsonObject
                            val facilityName = dataObject["facilityName"].asString
                            val address = dataObject["address"].asString

                            val phoneNumber = if (dataObject["phoneNumber"].isJsonNull) "정보없음"
                            else dataObject["phoneNumber"].asString

                            val operatingTime = if (dataObject["operatingTime"].isJsonNull) "정보없음"
                            else dataObject["operatingTime"].asString

                            val operatingDate = if (dataObject["operatingDate"].isJsonNull) "정보없음"
                            else dataObject["operatingDate"].asString

                            val latitude = dataObject["latitude"].asDouble
                            val longitude = dataObject["longitude"].asDouble

                            val soupKitchenDataObject = dataToJsonObject(facilityName, address, phoneNumber,
                                operatingTime, operatingDate, latitude, longitude)

                            val marker = MapPOIItem()
                            marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude,longitude)
                            marker.itemName = facilityName
                            marker.userObject = soupKitchenDataObject
                            marker.customCalloutBalloon
                            mapView.addPOIItem(marker)
                        }
                    } false -> failMessage.show()
                }
            }
        })
    }

    private fun dataToJsonObject(facilityName: String, address: String, phoneNumber: String,
    operatingTime:String, operatingDate:String, latitude: Double, longitude: Double): JSONObject {

        val soupKitchenDataObject = JSONObject()

        soupKitchenDataObject.put("facilityName",facilityName)
        soupKitchenDataObject.put("address",address)
        soupKitchenDataObject.put("phoneNumber",phoneNumber)
        soupKitchenDataObject.put("operatingTime",operatingTime)
        soupKitchenDataObject.put("operatingDate",operatingDate)
        soupKitchenDataObject.put("latitude",latitude)
        soupKitchenDataObject.put("longitude",longitude)

        return soupKitchenDataObject
    }

    private fun showDialog(infoObject: JsonObject, itemName: String, latitude: Double, longitude: Double){

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

        dialog.addressTextView.text = infoObject["address"].asString
        dialog.phoneNumberTextView.text = infoObject["phoneNumber"].asString
        dialog.operationDateTextView.text = infoObject["operatingDate"].asString
        dialog.operationTimeTextView.text = infoObject["operatingTime"].asString

        dialog.roadFindButton.setOnClickListener {

            val url = "https://map.kakao.com/link/to/$itemName,$latitude,$longitude"
            val nextIntent = Intent(Intent.ACTION_VIEW,Uri.parse(url))
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(nextIntent)
        }

        dialog.dialogCloseButton.setOnClickListener {
            dialog.dismiss()
        }
    }

}