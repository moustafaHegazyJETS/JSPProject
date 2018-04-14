/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package users;

import DBTables.DBConnection;
import DBTables.Product;
import DBTables.User;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;




/**
 *
 * @author Salma
 */
public class userProfile extends HttpServlet {
    
    String savePath;
    String uploadedImgProfileName;
    DBConnection dBConnection;
    String uniqueImgFileName;
    

    public userProfile() {
        
        dBConnection = new DBConnection();
        dBConnection.connect();
    }
    
   


  
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        
        //get products which bought by a specific user
        User user = (User) request.getSession().getAttribute("userInfo"); //loggedIn user
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        
//        if(request.getParameterMap().containsKey("imgParam"))
//        {   
//            
//            out.print(user.getImg());
//            
////            System.out.println("in img oif == >");
////            User userData = dBConnection.getAllUserData(user.getId());
////            Gson userDataJson = new Gson();
////            out.write(userDataJson.toJson(userData));
//        }
//        else{
//            ArrayList<Product> productsByUser = dBConnection.AllProductsByUser(user.getId());
//
//            Gson AllproductsbyUser = new Gson();
//            out.write(AllproductsbyUser.toJson(productsByUser));
//        
//        
//        }
        
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        PrintWriter out = response.getWriter();
        HttpSession sessions = request.getSession(false);
                
        User currentUser = (User) sessions.getAttribute("userInfo");


        if(request.getParameterMap().containsKey("emailField"))
        {
            
            System.out.println("in first if in servlet");
            User newUserData = new User();
        
            System.out.println("saaaaaaaaaaaa"+request.getParameter("emailField"));
            System.out.println("saaaaaaaaaaaa"+currentUser.getId());
            
            newUserData.setId(currentUser.getId());
            newUserData.setMoney(currentUser.getMoney());
            newUserData.setState(currentUser.getState());
            newUserData.setCreditCard(currentUser.getCreditCard());
            
            
            newUserData.setEmail(request.getParameter("emailField"));
            newUserData.setUserName(request.getParameter("userNameField"));
            newUserData.setMobile(request.getParameter("phoneField"));
            newUserData.setPassword(request.getParameter("passwordField"));
            newUserData.setImg(request.getParameter("img"));
            
            String res = updateUserInfoInDB(newUserData , request.getParameter("id"));
            
            if(res.equals("Done"))
            {
//                HttpSession session = request.getSession(true);
//                session.setAttribute("userInfo",newUserData);
                request.getSession(false).setAttribute("userInfo", newUserData);
            }
            
            out.print("Done");
            
        }else if(request.getParameterMap().containsKey("keyNumber")){
            
            String keyNumberVal = request.getParameter("keyNumber");
        
            int effectedRows = 0;
            
            if(keyNumberVal.equals("123456")){
                effectedRows = dBConnection.chargeAccount(currentUser.getId() , 1000);
                currentUser.setMoney(currentUser.getMoney()+1000);
            }
            else if(keyNumberVal.equals("654321")){
                effectedRows = dBConnection.chargeAccount(currentUser.getId() , 2000);
                currentUser.setMoney(currentUser.getMoney()+2000);
            }
            else if (keyNumberVal.equals("135792")){
                effectedRows = dBConnection.chargeAccount(currentUser.getId() , 3000);
                currentUser.setMoney(currentUser.getMoney()+3000);
            }
            
            
            if(effectedRows == 1)
            {
                request.getSession(false).setAttribute("userInfo", currentUser);
                out.print("Done");
            }
                
        
        }else{
            System.out.println("in second if in servlet");
            uploadedImgProfileName = saveImageInServer(request.getParameter("image-file"), request , response);
         
            if(!uploadedImgProfileName.equals(null))
            {
                saveUploadedImageInDB(uploadedImgProfileName , request , response);
                currentUser.setImg(uploadedImgProfileName);
                request.getSession(false).setAttribute("userInfo", currentUser);
                out.print(uploadedImgProfileName);
            }
            else
                System.out.println("something wrong happened when saving img on server ");
        
        }
    }
    

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String saveImageInServer(String imgName ,HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException{

        try {
            
            PrintWriter out = response.getWriter();
            
            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();
            
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                System.out.println("Entered while loop ==>");
                FileItem item = iter.next();
                if (item.isFormField()) {
                   
                    String name = item.getFieldName();
                    String value = item.getString();
                } else {
                    System.out.println("in else case");
                    if (!item.isFormField()) {
                        try {
                            String fileNameWithoutExtention = item.getName().substring(0 , item.getName().lastIndexOf("."));
                            String extention = item.getName().substring(item.getName().lastIndexOf("."));
                            uniqueImgFileName =  fileNameWithoutExtention + new Date().getTime() + extention;
                            savePath = getServletContext().getRealPath("")+ "\\"+ "images";
                            item.write(new File(savePath + File.separator + uniqueImgFileName));
                            return "images//"+uniqueImgFileName;
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                  
                        }
                    }
                }
            }
        } catch (FileUploadException ex) {
            System.out.println("Exception while saving uploaded image in catch section with error \n");
            ex.printStackTrace();
        }
//    
        return null;
    
    }

    private void saveUploadedImageInDB(String uploadedImage , HttpServletRequest request, HttpServletResponse response) {
        
        //get current user from session 
        User user = (User) request.getSession().getAttribute("userInfo");
        
        //update current user data in db using his ID
        int effectedRows = dBConnection.updateUserImage(uploadedImage , user.getId());
        System.out.println("the user id is " + user.getUserName());
        
        if(effectedRows != 0)
            System.out.println("image updated successfully with no of effected rows " + effectedRows);
        else
            System.out.println("something wrong happened while updating user image [in userProfile servlet]");
    }

    private String updateUserInfoInDB(User newUserData , String currentUserId) {
        
        //update current user data in db using his ID
        int effectedRows = 0;
        try{
        effectedRows = dBConnection.updateUserProfile(newUserData , currentUserId);
        }
        
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        if(effectedRows == 1)
        {
            System.out.println("data updated successfully with no of effected rows " + effectedRows);
            return "Done";
        }
        else{
            System.out.println("something wrong happened while updating user info [in userProfile servlet]");
            return "Error";
        }
    }
}
