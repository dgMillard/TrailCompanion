//
//  HomeVC.swift
//
//  Created by MAC-186 on 4/8/16.
//  Copyright © 2016 Kode. All rights reserved.
//

import UIKit
import Mapbox

class HomeVC: BaseViewController, MGLMapViewDelegate {

    
    //my table view
    @IBOutlet weak var home_table: UITableView!
    
    //my map
    @IBOutlet weak var home_map: MGLMapView!
    
    //read json
    /*
    struct Waypoints {
        let siteName: String
        let description: String
        //let version: String
        let topLeft: (latitude: Double, longitude: Double)
    }
    
    extension My_waypoint {
        init?(json: [String: Any]) {
            guard let siteName = json["siteName"] as? String,
                let topLeftJSON = json["topLeft"] as? [String: Double],
                let latitude = coordinatesJSON["latitude"],
                let longitude = coordinatesJSON["longitude"],
                let description = json["description"] as? String
                else {
                    return nil
            }
            
            
            self.name = siteName
            self.topLeftJSON = (latitude, longitude)
            self.des = description
        }
    }*/
    

    
    
    
    
    //--------------------------------------
    override func viewDidLoad() {
        super.viewDidLoad()
        addSlideMenuButton()
        // Do any additional setup after loading the view.
        
        home_map.delegate = self
        
        let point = MGLPointAnnotation()
        point.coordinate = CLLocationCoordinate2D(latitude: 44.564178, longitude: -123.279306)
        point.title = "OSU"
        point.subtitle = "Corvallis, OR"
        
        home_map.addAnnotation(point)
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

    
    
    
    
    
    
    
    



}









