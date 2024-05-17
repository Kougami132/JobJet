package cn.kougami.platinum;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Zhipin {
    private String loginUrl = "https://www.zhipin.com/web/user/?ka=header-login";
    private String baseUrl = "https://www.zhipin.com/web/geek/job?scale=301,302,303,304&query=%s&city=%s&page=";
    private List<String> blackList;
    private ChromeDriver driver;
    private WebDriverWait webDriverWait;

    public Zhipin(String query, String city, List blackList) {
        baseUrl = String.format(baseUrl, query, city);
        this.blackList = blackList;
    }

    public void start() {
        log.info("开始BOSS直聘打招呼");
        driver = new ChromeDriver();
        webDriverWait = new WebDriverWait(driver, 10);
        login();
        for (int page = 1; page <= 10; page ++)
            if (!chat(baseUrl + page)) break;
        log.info("今日沟通人数已达上限，请明天再试");
        driver.close();
    }

    @SneakyThrows
    private Boolean chat(String url) {
        driver.get(url);
        wait("job-card-body clearfix");

        List<String> list = new ArrayList<>();
        findBatch("job-card-body").forEach(o -> {
            // 检查黑名单
            if (!isInBlackList(find(o, "company-name").getText()))
                list.add(find(o, "job-card-left").getAttribute("href"));
        });

        for (String i: list) {
            driver.get(i);
            wait("btn-startchat");
            WebElement button = find("btn-startchat");
            if (button.getText().equals("立即沟通")) {
                button.click();
                wait("dialog-con");
                if (find("dialog-con").getText().contains("已达上限")) return false;

                String com = findBatch("company-info").get(1).getText();
                String title = find("name").getText();
                log.info("已投递 {} | {}", com, title);
                Thread.sleep(1500);
            }
        }
        return true;
    }
    private void login() {
        driver.get(loginUrl);
        wait("btn-sign-switch");
        driver.findElementByClassName("btn-sign-switch").click();
        log.info("等待扫码登陆...");
        wait("recommend-job-btn", 3600);
    }
    private void wait(String className) {
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='" + className + "']")));
    }
    private void wait(String className, Integer timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='" + className + "']")));
    }
    private WebElement find(String className) {
        return driver.findElement(By.cssSelector("[class*='" + className + "']"));
    }
    private WebElement find(WebElement element, String className) {
        return element.findElement(By.cssSelector("[class*='" + className + "']"));
    }
    private List<WebElement> findBatch(String className) {
        return driver.findElements(By.cssSelector("[class*='" + className + "']"));
    }
    private Boolean isInBlackList(String s) {
        for (String i: blackList)
            if (s.contains(i)) return true;
        return false;
    }


}
