import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams, ToastController, Platform } from 'ionic-angular';
import { GoogleMap, GoogleMaps, Marker, GoogleMapsEvent, GoogleMapsAnimation, MyLocation } from '@ionic-native/google-maps';

/**
 * Generated class for the GoogleMapPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-google-map',
  templateUrl: 'google-map.html',
})
export class GoogleMapPage {
  map: GoogleMap;

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public toastCtrl: ToastController,
    public platform: Platform
  ) {
  }

  ionViewDidLoad() {
    this.platform.ready().then(() => {
      this.loadMap();
    });
    console.log('ionViewDidLoad GoogleMapPage');
  }

  loadMap() {
    this.map = GoogleMaps.create('map_canvas');
  }
}
