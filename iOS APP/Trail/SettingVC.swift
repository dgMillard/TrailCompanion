//
//  SettingVC.swift
//  Trail
//
//  Created by Jiawei on 2017/4/29.
//  Copyright © 2017年 OSU. All rights reserved.
//

//import Foundation
import UIKit

class SettingVC: UITableViewController, URLSessionDownloadDelegate, UIDocumentInteractionControllerDelegate {
    @IBOutlet weak var to_set: UIButton!
    @IBOutlet weak var down1: UIButton!

    
    var downloadTask: URLSessionDownloadTask!
    var backgroundSession: URLSession!
    
    
    @IBAction func junp_set(_ sender: Any) {
        if let url = URL(string:UIApplicationOpenSettingsURLString) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }
    
    // download the json file from the url
    @IBAction func act_down_1(_ sender: Any) {
        let url = URL(string: "http://www.kaleidosblog.com/tutorial/tutorial.json")!
        downloadTask = backgroundSession.downloadTask(with: url)
        downloadTask.resume()
    }
    /*
    @IBAction func act_down_2(_ sender: Any) {
        let url2 = URL(string: "http://web.engr.oregonstate.edu/~liujiaw/testJson")!
        downloadTask = backgroundSession.downloadTask(with: url2)
        downloadTask.resume()
    }
    */
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let backgroundSessionConfiguration = URLSessionConfiguration.background(withIdentifier: "backgroundSession")
        backgroundSession = Foundation.URLSession(configuration: backgroundSessionConfiguration, delegate: self, delegateQueue: OperationQueue.main)
        //progressView.setProgress(0.0, animated: false)
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func showFileWithPath(path: String){
        let isFileFound:Bool? = FileManager.default.fileExists(atPath: path)
        if isFileFound == true{
            let viewer = UIDocumentInteractionController(url: URL(fileURLWithPath: path))
            viewer.delegate = self
            viewer.presentPreview(animated: true)
        }
    }
    
    //MARK: URLSessionDownloadDelegate
    // 1
    func urlSession(_ session: URLSession,
                    downloadTask: URLSessionDownloadTask,
                    didFinishDownloadingTo location: URL){
        
        let path = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.userDomainMask, true)
        let documentDirectoryPath:String = path[0]
        let fileManager = FileManager()
        let destinationURLForFile = URL(fileURLWithPath: documentDirectoryPath.appendingFormat("/tutorial.json"))
        
        if fileManager.fileExists(atPath: destinationURLForFile.path){
            showFileWithPath(path: destinationURLForFile.path)
        }
        else{
            do {
                try fileManager.moveItem(at: location, to: destinationURLForFile)
                // show file
                showFileWithPath(path: destinationURLForFile.path)
            }catch{
                print("An error occurred while moving file to destination url")
            }
        }
    }
    // 2
    func urlSession(_ session: URLSession,
                    downloadTask: URLSessionDownloadTask,
                    didWriteData bytesWritten: Int64,
                    totalBytesWritten: Int64,
                    totalBytesExpectedToWrite: Int64){
        //progressView.setProgress(Float(totalBytesWritten)/Float(totalBytesExpectedToWrite), animated: true)
    }
    
    //MARK: URLSessionTaskDelegate
    func urlSession(_ session: URLSession,
                    task: URLSessionTask,
                    didCompleteWithError error: Error?){
        downloadTask = nil
        //progressView.setProgress(0.0, animated: true)
        if (error != nil) {
            print(error!.localizedDescription)
        }else{
            print("The task finished transferring data successfully")
        }
    }
    
    //MARK: UIDocumentInteractionControllerDelegate
    func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController
    {
        return self
    }
    
    /*---------------------
     //parse json example
    func extract_json(_ data: Data)
    {
        
        
        let json: Any?
        
        do
        {
            json = try JSONSerialization.jsonObject(with: data, options: [])
        }
        catch
        {
            return
        }
        
        guard let data_list = json as? NSArray else
        {
            return
        }
        
        
        if let countries_list = json as? NSArray
        {
            for i in 0 ..< data_list.count
            {
                if let country_obj = countries_list[i] as? NSDictionary
                {
                    if let country_name = country_obj["country"] as? String
                    {
                        if let country_code = country_obj["code"] as? String
                        {
                            TableData.append(country_name + " [" + country_code + "]")
                        }
                    }
                }
            }
        }
        
        
        
        DispatchQueue.main.async(execute: {self.do_table_refresh()})
        
    }
    
    func do_table_refresh()
    {
        self.tableView.reloadData()
        
    }
    */
    
}
