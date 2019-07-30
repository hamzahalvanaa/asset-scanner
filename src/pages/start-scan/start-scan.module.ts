import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { StartScanPage } from './start-scan';
import { LottieAnimationViewModule } from 'ng-lottie';

@NgModule({
  declarations: [
    StartScanPage,
  ],
  imports: [
    IonicPageModule.forChild(StartScanPage),
    LottieAnimationViewModule
  ],
})
export class StartScanPageModule {}
