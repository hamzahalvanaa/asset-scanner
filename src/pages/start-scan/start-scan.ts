import { Component, NgZone } from '@angular/core';
import { IonicPage, NavController, NavParams, Platform } from 'ionic-angular';
declare var window: any;
declare var UhfrPlugin: any;

/**
 * Generated class for the StartScanPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-start-scan',
  templateUrl: 'start-scan.html',
})
export class StartScanPage {
  public lottieConfig: Object;
  public animation: any;
  public firmware: any;
  public tidList: any;

  constructor(
    public navCtrl: NavController,
    public navParams: NavParams,
    public zone: NgZone,
    public platform: Platform
  ) {
    this.lottieConfig = {
      path: 'https://assets5.lottiefiles.com/datafiles/imdF9U9Xrs5M9TyStloHZCZbitqk6mIVt7JgxIvo/Scan sensor/scan sensor.json',
      renderer: 'canvas',
      autoplay: true,
      loop: true
    };
  }

  ionViewDidLoad() {
    this.platform.ready().then(() => {
      this.setPower();
    });
    console.log('ionViewDidLoad StartScanPage');
  }

  handleAnimation(animation) {
    this.animation = animation;
    this.animation.setSpeed(0.8);
    this.animation.play();
  }

  openMap() {
    this.navCtrl.push('GoogleMapPage');
  }

  setPower() {
    var uhfrPlugin = new UhfrPlugin();
    uhfrPlugin.show(
      'Nek ko weruh tulisan kie berarti wis dadi :)',
      function (msg) {
        console.log(msg);
      },
      function (err) {
        console.log(err);
      }
    );
  }

  getPower() {
    var success = function (message) {
      alert(message);
    }

    var failure = function () {
      alert("Error calling Hello Plugin");
    }

    var uhfrPlugin = new UhfrPlugin();
    uhfrPlugin.greet("World", success, failure);
  }

  readCard() {
    const that = this;
    window.C4ApiCordovaPlugin.startInventory((result: any) => {
      that.zone.run(() => {
        that.tidList = result;
        alert('Inventory result : ' + result);
        (<any>window).C4ApiCordovaPlugin.stopInventory();
      });
    }, (error: string) => {
      alert('startInventory error: ' + error);

    });
    // let obj = { site: 20 };
    // cordova.plugins.UHF.readCard(
    //   obj, (success) => {
    //     alert(JSON.stringify(success));
    //   }, err => {
    //     alert(JSON.stringify(err));
    //   }
    // )
  }

  searchCard() {
    const that = this;
    window.plugins.C4GunApi.getFirmware((result: ArrayBuffer) => {
      alert(JSON.stringify('Firmware: ' + result));
      if (result) {
        that.zone.run(() => {
          that.firmware = String.fromCharCode.apply(null, new Uint8Array(result));
        });
        alert('Firmware converted: ' + that.firmware);
      }
    }, (error: string) => {
      alert(JSON.stringify('Firmware error: ' + error));
      that.firmware = error;
    });
    // cordova.plugins.UHF.searchCard(
    //   (success) => {
    //     alert(JSON.stringify(success));
    //   }, err => {
    //     alert(JSON.stringify(err));
    //   }
    // )
  }

}
