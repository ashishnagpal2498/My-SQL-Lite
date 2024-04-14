import java.util.Scanner;
import java.lang.System;
/**
 * Manages user authentication and authorization for the system.
 */
public class Authentication {
    private String username;
    private String password;
    private String hashPassword;

    /**
     * Default constructor for the Authentication class.
     */
    Authentication(){
    }
    /**
     * Logs in the user with provided credentials.
     *
     * @return The username if login is successful; otherwise returns "null".
     */
    public String login(){
        try {
            Users user = new Users();
            if (!user.validateUser(username, hashPassword)) {
                System.out.println("Invalid Credentials Entered");
                return "null";
            }
            System.out.println("Login Successful ");
            System.out.println("Hello "+ username);

        } catch (Exception e){
            System.out.println(e);
        }
        return username;
    }
    /**
     * Registers a new user.
     */
    public void signUp (){
        Users newUser = new Users();
        newUser.createUser(username,hashPassword);
        System.out.println("User Created Successfully");
    }
    /**
     * Authenticates the user for login or signup based on the authentication option.
     *
     * @param authOption The authentication option: 1 for login, 2 for signup.
     * @return The username if successful login/signup; otherwise returns "null".
     */
    public String authenticate(int authOption){
        DBUtil utils = new DBUtil();
        Scanner readInput = new Scanner(System.in);
        if (authOption == 1) {
            System.out.println("Login User");
        } else {
            System.out.println("Signup User");
        }
        System.out.println("Enter Username or press 7 to go to main menu");
        username = readInput.nextLine();
        if (username.equals("7")) {
            return "null";
        }
        System.out.println("Enter Password");
        password = readInput.nextLine();
        hashPassword = utils.hashPassword(password);
        int enteredCaptcha;
        int captcha;
        do {
            captcha = utils.generateCaptcha();
            System.out.println("Captcha : " + captcha);
            System.out.println("Please enter the captcha below: ");
            enteredCaptcha = readInput.nextInt();
        }while(enteredCaptcha != captcha);
        if(authOption == 1){
            return login();
        }
        else{
            signUp();
        }
        return "null";
    }
}
