//
//  WayPointTableViewController.swift
//  Trail
//
//  Created by Jiawei on 2017/4/29.
//  Copyright © 2017年 OSU. All rights reserved.
//

import UIKit

class RestaurantTableViewController: UITableViewController {

    var restaurants:[Restaurant] = [
        Restaurant(name: "Way Point 1", type: "Lake", location: "This is the description of waypoint 1", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 2", type: "Forest", location: "There is a beatiful forest here", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 3", type: "Lake", location: "There is a lovely lake here", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 4", type: "Lake", location: "You can fishing here", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 5", type: "Landscape", location: "The landscape is beatiful", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 6", type: "Lake", location: "Here is another lake", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 7", type: "Forest", location: "HUNTER×HUNTER", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 8", type: "Lake", location: "The third beatiful lake!", phone: "xxx", image: "huaji.jpg", isVisited: false),
        Restaurant(name: "Way Point 9", type: "Tree House", location: "New waypoint, a tree house, you can hava a party here", phone: "xxx", image: "huaji.jpg", isVisited: false),
        
    ]
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Remove the title of the back button
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
        
        navigationController?.hidesBarsOnSwipe = false
        
        // Enable Self Sizing Cells
        tableView.estimatedRowHeight = 80.0
        tableView.rowHeight = UITableViewAutomaticDimension
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.hidesBarsOnSwipe = false
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return restaurants.count
    }

    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cellIdentifier = "Cell"
        let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as! RestaurantTableViewCell
        
        // Configure the cell...
        cell.nameLabel.text = restaurants[indexPath.row].name
        cell.thumbnailImageView.image = UIImage(named: restaurants[indexPath.row].image)
        cell.locationLabel.text = restaurants[indexPath.row].location
        cell.typeLabel.text = restaurants[indexPath.row].type

        cell.accessoryType = restaurants[indexPath.row].isVisited ? .checkmark : .none
        
        return cell
    }
    
    
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        
        if editingStyle == .delete {
            // Delete the row from the data source
            restaurants.remove(at: indexPath.row)
        }
        
        tableView.deleteRows(at: [indexPath], with: .fade)
    }

    override func tableView(_ tableView: UITableView, editActionsForRowAt indexPath: IndexPath) -> [UITableViewRowAction]? {
        
        // Social Sharing Button
        let shareAction = UITableViewRowAction(style: UITableViewRowActionStyle.default, title: "Share", handler: { (action, indexPath) -> Void in
            
            let defaultText = "Just checking in at " + self.restaurants[indexPath.row].name
            
            if let imageToShare = UIImage(named: self.restaurants[indexPath.row].image) {
                let activityController = UIActivityViewController(activityItems: [defaultText, imageToShare], applicationActivities: nil)
                self.present(activityController, animated: true, completion: nil)
            }
        })
        
        // Delete button
        //let deleteAction = UITableViewRowAction(style: UITableViewRowActionStyle.default, title: "Delete",handler: { (action, indexPath) -> Void in
            
            // Delete the row from the data source
          //  self.restaurants.remove(at: indexPath.row)
            
            //self.tableView.deleteRows(at: [indexPath], with: .fade)
        //})
        
        shareAction.backgroundColor = UIColor(red: 48.0/255.0, green: 173.0/255.0, blue: 99.0/255.0, alpha: 1.0)
        //deleteAction.backgroundColor = UIColor(red: 202.0/255.0, green: 202.0/255.0, blue: 203.0/255.0, alpha: 1.0)
        
        //return [deleteAction, shareAction]
        return [shareAction]
    }
    
    // MARK: -
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "showRestaurantDetail" {
            if let indexPath = tableView.indexPathForSelectedRow {
                let destinationController = segue.destination as! RestaurantDetailViewController
                destinationController.restaurant = restaurants[indexPath.row]
            }
        }
    }
    
}
