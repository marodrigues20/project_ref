package sample.multimodule.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import sample.multimodule.domain.entity.Account;
import sample.multimodule.service.api.AccountService;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

@Controller
public class WelcomeController
{
    @Value("${application.message:Hello World}")
    private String message = "Hello World";
    @Autowired
    protected AccountService accountService;
    @RequestMapping("/")
    public String welcome(Map<String, Object> model) throws AccountNotFoundException {
// Trying to obtain 23 account
        Account account = accountService.findOne("23");
        if(account == null){
// If there's some problem creating account, return show view with error status
            model.put("message", "Error getting account!");
            model.put("account", "");
            return "welcome/show";
        }
// Return show view with 23 account info
        String accountInfo = "Your account number is ".concat(account.getNumber());
        model.put("message", this.message);
        model.put("account", accountInfo);
        return "welcome/show";
    }
    @RequestMapping("foo")
    public String foo(Map<String, Object> model) {
        throw new RuntimeException("Foo");
    }
}
