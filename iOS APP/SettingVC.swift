//
//  Setting.swift
//  AKSwiftSlideMenu
//
//  Created by Jiawei on 2017/2/7.
//  Copyright © 2017年 Kode. All rights reserved.
//

import UIKit

class SettingVC: BaseViewController {
    @IBOutlet weak var go_setting: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        addSlideMenuButton()
        // Do any additional setup after loading the view.
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func showalert(_ sender: Any) {
        if let url = URL(string:UIApplicationOpenSettingsURLString) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
        
    }
        
    
}

