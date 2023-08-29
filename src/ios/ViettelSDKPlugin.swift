

import GoSignSDKLite

@objc(ViettelSDKPlugin)
class ViettelSDKPlugin: CDVPlugin {

    @objc(coolMethod:)
    func coolMethod(command: CDVInvokedUrlCommand) {
        
        var pluginResult: CDVPluginResult? = nil
        if let echo = command.arguments.first as? String, !echo.isEmpty {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: echo)
        } else {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
        }
        
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }
    

    @objc(getDeviceId:)
    func getDeviceId(command: CDVInvokedUrlCommand){
        var pluginResult: CDVPluginResult? = nil
        let deviceId = API.getDeviceId()
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs:deviceId)
        print(deviceId)
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }
    
    @objc(registerDevice:)
        func registerDevice(command: CDVInvokedUrlCommand) {
            print("register kitrm tra alll: ", command.arguments)
            var pluginResult: CDVPluginResult? = nil
            if let echo = command.arguments.first as? String, !echo.isEmpty,
               let userId = command.arguments?[1] as? String, !userId.isEmpty
            {
                DispatchQueue.global(qos: .background).async {
                    API.setUserId(userId)
                    API.registerDevice(authenToken: echo) { result in
                        switch result {
                        case .success(let res):
                            print("kiemtra: res:", res)
                            DispatchQueue.main.async {
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "success")
                                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                            }
                        case .failure(let error):
                            print("kiemtra: error", error.localizedDescription)
                            DispatchQueue.main.async {
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(error.localizedDescription)")
                                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                            }
                        }
                    }
                }
            } else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid argument")
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            }
        }
    
    @objc(authorisationPendingRequest:)
        func authorisationPendingRequest(command: CDVInvokedUrlCommand) {
            var pluginResult: CDVPluginResult? = nil
            var arguments = command.arguments as? [Any]
            var request = arguments?.first as? String
            var access_token = arguments?[1] as? String
            var transaction_id = arguments?[2] as? String
            print("kiemtra alll: ",arguments)
            print("kiemtra request:", command.arguments.first as? String)
            print("kiemtra access_token:", command.arguments?[1] as? String)
            print("kiemtra transactionID:", command.arguments?[2] as? String)
            if let request = command.arguments.first as? String, !request.isEmpty,
               let access_token  = command.arguments?[1] as? String, !access_token.isEmpty,
               let transactionID = command.arguments?[2] as? String, !transactionID.isEmpty,
               let userId = command.arguments?[3] as? String, !userId.isEmpty
            {
                API.setUserId(userId)
                print("request:", request)
                print("access_token:", access_token)
                print("transactionID:", transactionID)
                DispatchQueue.global(qos: .background).async {
                    API.authoriseaPendingRequest(authenToken: access_token, pendingAuthorisationAPIResponse: PendingAuthorisationAPIResponse(transactionID: transactionID, request: request, hashAlgorithm: "SHA256")) { result in
                        switch result {
                        case .success(let res):
                            print("kiemtra: res:", res)
                            DispatchQueue.main.async {
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "success")
                                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                            }
                        case .failure(let failure):
                            let error = failure as! ServerResponseError
                            let response = "Failure: \(error.message)"
                            print("kiemtra: error", error)
                            print("test rrrrr: ", response)
                            DispatchQueue.main.async {
                                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(error.localizedDescription)")
                                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                            }
                        }
                    }
                }
            } else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid argument")
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            }
        }
    
}
