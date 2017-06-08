//
//  WayPointTableViewController.swift
//  Trail
//
//  Copyright © 2017年 OSU. All rights reserved.
//

import UIKit

class RestaurantTableViewController: UITableViewController {

    var restaurants:[Restaurant] = [
        Restaurant(name: "Kelly Engineering Building", type: "Building", location: "Hey, That's where you are right now! That's some crazy stuff right there.", phone: "xxx", image: "kelly.jpg", isVisited: false),
        Restaurant(name: "Robotics Expo Area", type: "Area", location: "Go check out the awesome robots over here during expo! One of the highlights for sure.", phone: "xxx", image: "robots.jpg", isVisited: false),
        Restaurant(name: "MU", type: "Building", location: "The MU always smells like Panda Express. No one talks about it, but we all know it's true.", phone: "xxx", image: "MU.jpg", isVisited: false),
        Restaurant(name: "Valley Library", type: "Building", location: "A very large portion of this application was written inside this building.", phone: "xxx", image: "library.jpg", isVisited: false),
        Restaurant(name: "Sizzle Pie", type: "Food", location: "Great place to start the night out. Draft beer and pizza paired with a great atmosphere.", phone: "xxx", image: "pizza.jpg", isVisited: false),
        Restaurant(name: "Ground Kontrol", type: "Bar", location: "This is a really nice bar arcade. Full bar and retro video games make for a very good time.", phone: "xxx", image: "mm.jpg", isVisited: false),
        Restaurant(name: "Jimmy Mak's", type: "jazz", location: "This is (or was) THE place to see some live jazz at any day of the week in Portland. Talented artists and great location. Check out the balcony!", phone: "xxx", image: "jazz.jpg", isVisited: false),
        Restaurant(name: "Golden Gate Park Conservatory", type: "Park", location: "As you venture into one of the first buildings situated in Golden Gate Park, you will encounter the oldest remaining municipal wooden conservatory in the United States. As the first public structures of its kind in the country, the Conservatory of Flowers serves as a safe haven for thought and imagination as visitors browse about some of the most exotic-looking blooms, sometimes presenting the beauty of colorful rarities. Highly praised in the world of history, architecture, engineering, and nature, the Conservatory of Flowers has been placed on the National Register of Historic Places, and is considered an intensely valued landmark in San Francisco.", phone: "xxx", image: "golden_gate_park.jpg", isVisited: false),
        Restaurant(name: "Walt Disney Family Museum", type: "Museum", location: "The Walt Disney Family Museum is an American museum that features the life and legacy of Walt Disney. The museum is located in The Presidio of San Francisco, part of the Golden Gate National Recreation Area in San Francisco.", phone: "xxx", image: "mm.jpg", isVisited: false),
        Restaurant(name: "Get Lunch Here!", type: "Food", location: "Best burritos of your life! go here.", phone: "xxx", image: "resturant.jpg", isVisited: false),
        Restaurant(name: "Fort Point", type: "Landscape", location: "Fort Point has stood guard at the narrows of the Golden Gate for over 150 years. The Fort has been called the pride of the Pacific, the Gibraltar of the West Coast, and one of the most perfect models of masonry in America. When construction began during the height of the California Gold Rush, Fort Point was planned as the most formidable deterrence America could offer to a naval attack on California. Although its guns never fired a shot in anger, the Fort at Fort Point as it was originally named has witnessed Civil War, obsolescence, earthquake, bridge construction, reuse for World War II, and preservation as a National Historic Site.", phone: "xxx", image: "fort_point.jpg", isVisited: false),
        
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
