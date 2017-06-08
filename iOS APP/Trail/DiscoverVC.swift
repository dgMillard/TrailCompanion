//
//  DiscoverVC.swift
//  Trail
//
//  Created by Jiawei on 2017/4/29.
//  Copyright © 2017年 OSU. All rights reserved.
//

//import Foundation
import UIKit
import Mapbox


class MyCustomPointAnnotation: MGLPointAnnotation {
    var willUseImage: Bool = false
}


class DiscoverVC: UIViewController, MGLMapViewDelegate {
    @IBOutlet weak var mapview: MGLMapView!
    var mapView: MGLMapView!
    var progressView: UIProgressView!
    
    //@IBOutlet var mapview: MGLMapView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        mapview.delegate = self
        
        // add waypoints on the map
        let pointA = MyCustomPointAnnotation()
        pointA.coordinate = CLLocationCoordinate2D(latitude: 37.769048, longitude: -122.473269)
        pointA.title = "Golden Gate Park Conservatory"
        pointA.subtitle = "San Fransisco"
        
        let pointB = MyCustomPointAnnotation()
        pointB.coordinate = CLLocationCoordinate2D(latitude: 37.799167, longitude: -122.458506)
        pointB.title = "Walt Disney Family Museum"
        pointB.subtitle = "San Fransisco"
        
        let pointC = MyCustomPointAnnotation()
        pointC.coordinate = CLLocationCoordinate2D(latitude: 37.778682, longitude: -122.478933)
        pointC.title = "Get Lunch Here!"
        pointC.subtitle = "San Fransisco"
        
        let pointD = MyCustomPointAnnotation()
        pointD.coordinate = CLLocationCoordinate2D(latitude: 37.809068, longitude: -122.477045)
        pointD.title = "Fort Point"
        pointD.subtitle = "San Fransisco"
        
        let pointE = MyCustomPointAnnotation()
        pointE.coordinate = CLLocationCoordinate2D(latitude: 45.522914, longitude: -122.659333)
        pointE.title = "Sizzle Pie"
        pointE.subtitle = "Portland Nightlife"
 
        let pointF = MyCustomPointAnnotation()
        pointF.coordinate = CLLocationCoordinate2D(latitude: 45.523889, longitude: -122.675502)
        pointF.title = "Ground Kontrol"
        pointF.subtitle = "Portland Nightlife"
        
        let pointG = MyCustomPointAnnotation()
        pointG.coordinate = CLLocationCoordinate2D(latitude: 45.524529, longitude: -122.681268)
        pointG.title = "Jimmy Mak's"
        pointG.subtitle = "Portland Nightlife"
        
        let pointH = MyCustomPointAnnotation()
        pointH.coordinate = CLLocationCoordinate2D(latitude: 44.566861, longitude: -123.278461)
        pointH.title = "Kelly Engineering Building"
        pointH.subtitle = "Oregon State Campus"
        
        let pointI = MyCustomPointAnnotation()
        pointI.coordinate = CLLocationCoordinate2D(latitude: 44.567255, longitude: -123.277925)
        pointI.title = "Robotics Expo Area"
        pointI.subtitle = "Oregon State Campus"
        
        let pointJ = MyCustomPointAnnotation()
        pointJ.coordinate = CLLocationCoordinate2D(latitude: 44.565266, longitude: -123.278914)
        pointJ.title = "MU"
        pointJ.subtitle = "Oregon State Campus"
        
        let pointK = MyCustomPointAnnotation()
        pointK.coordinate = CLLocationCoordinate2D(latitude: 44.565623, longitude: -123.276015)
        pointK.title = "Valley Library"
        pointK.subtitle = "Oregon State Campus"
        
        
        let myPlaces = [pointA, pointB, pointC, pointD, pointE, pointF, pointG, pointH, pointI, pointJ, pointK]
        mapview.addAnnotations(myPlaces)
        
        mapview.setCenter(CLLocationCoordinate2D(latitude: 44.564174, longitude: -123.279306), zoomLevel: 13, animated: false)
        mapview.userTrackingMode = .follow
        
        
        NotificationCenter.default.addObserver(self, selector: #selector(offlinePackProgressDidChange), name: NSNotification.Name.MGLOfflinePackProgressChanged, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(offlinePackDidReceiveError), name: NSNotification.Name.MGLOfflinePackError, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(offlinePackDidReceiveMaximumAllowedMapboxTiles), name: NSNotification.Name.MGLOfflinePackMaximumMapboxTilesReached, object: nil)
        
    }
    
    // Note: You can remove this method, which lets you customize low-memory behavior.
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func mapView(mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        // Always try to show a callout when an annotation is tapped.
        return true
    }
    
    // Or, if you’re using Swift 3 in Xcode 8.0, be sure to add an underscore before the method parameters:
    func mapView(_ mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        // Always try to show a callout when an annotation is tapped.
        return true
    }
    
    //******************
    //offline map
    //******************
    func mapViewDidFinishLoadingMap(_ mapView: MGLMapView) {
        // Start downloading tiles and resources for z13-16.
        startOfflinePackDownload()
    }
    
    deinit {
        // Remove offline pack observers.
        NotificationCenter.default.removeObserver(self)
    }
    
    func startOfflinePackDownload() {
        // Create a region that includes the current viewport and any tiles needed to view it when zoomed further in.
        // Because tile count grows exponentially with the maximum zoom level, you should be conservative with your `toZoomLevel` setting.
        let region = MGLTilePyramidOfflineRegion(styleURL: mapview.styleURL, bounds: mapview.visibleCoordinateBounds, fromZoomLevel: mapview.zoomLevel, toZoomLevel: 16)
        
        // Store some data for identification purposes alongside the downloaded resources.
        let userInfo = ["name": "My Offline Pack"]
        let context = NSKeyedArchiver.archivedData(withRootObject: userInfo)
        
        // Create and register an offline pack with the shared offline storage object.
        
        MGLOfflineStorage.shared().addPack(for: region, withContext: context) { (pack, error) in
            guard error == nil else {
                // The pack couldn’t be created for some reason.
                print("Error: \(error?.localizedDescription ?? "unknown error")")
                return
            }
            
            // Start downloading.
            pack!.resume()
        }
        
    }
    
    // MARK: - MGLOfflinePack notification handlers
    
    func offlinePackProgressDidChange(notification: NSNotification) {
        // Get the offline pack this notification is regarding,
        // and the associated user info for the pack; in this case, `name = My Offline Pack`
        if let pack = notification.object as? MGLOfflinePack,
            let userInfo = NSKeyedUnarchiver.unarchiveObject(with: pack.context) as? [String: String] {
            let progress = pack.progress
            // or notification.userInfo![MGLOfflinePackProgressUserInfoKey]!.MGLOfflinePackProgressValue
            let completedResources = progress.countOfResourcesCompleted
            let expectedResources = progress.countOfResourcesExpected
            
            // Calculate current progress percentage.
            let progressPercentage = Float(completedResources) / Float(expectedResources)
            
            // Setup the progress bar.
            /*
            if progressView == nil {
                progressView = UIProgressView(progressViewStyle: .default)
                let frame = view.bounds.size
                progressView.frame = CGRect(x: frame.width / 4, y: frame.height * 0.75, width: frame.width / 2, height: 10)
                view.addSubview(progressView)
            } */
            
            //progressView.progress = progressPercentage
            
            // If this pack has finished, print its size and resource count.
            if completedResources == expectedResources {
                let byteCount = ByteCountFormatter.string(fromByteCount: Int64(pack.progress.countOfBytesCompleted), countStyle: ByteCountFormatter.CountStyle.memory)
                print("Offline pack “\(userInfo["name"] ?? "unknown")” completed: \(byteCount), \(completedResources) resources")
                // create the alert
                let alert = UIAlertController(title: "Offline Map", message: "\(byteCount) download sucessfully. ", preferredStyle: UIAlertControllerStyle.alert)
                // add an action (button)
                alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil))
                // show the alert
                self.present(alert, animated: true, completion: nil)
                
            } else {
                // Otherwise, print download/verification progress.
                print("Offline pack “\(userInfo["name"] ?? "unknown")” has \(completedResources) of \(expectedResources) resources — \(progressPercentage * 100)%.")
            }
        }
    }
    
    func offlinePackDidReceiveError(notification: NSNotification) {
        if let pack = notification.object as? MGLOfflinePack,
            let userInfo = NSKeyedUnarchiver.unarchiveObject(with: pack.context) as? [String: String],
            let error = notification.userInfo?[MGLOfflinePackUserInfoKey.error] as? NSError {
            print("Offline pack “\(userInfo["name"] ?? "unknown")” received error: \(error.localizedFailureReason ?? "unknown error")")
        }
    }
    
    func offlinePackDidReceiveMaximumAllowedMapboxTiles(notification: NSNotification) {
        if let pack = notification.object as? MGLOfflinePack,
            let userInfo = NSKeyedUnarchiver.unarchiveObject(with: pack.context) as? [String: String],
            let maximumCount = (notification.userInfo?[MGLOfflinePackUserInfoKey.maximumCount] as AnyObject).uint64Value {
            print("Offline pack “\(userInfo["name"] ?? "unknown")” reached limit of \(maximumCount) tiles.")
        }
    }
    
    
    

}



