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
import java.util.Set;

@Slf4j
public class Zhaopin {
    private String loginUrl = "https://passport.zhaopin.com/login";
    private String baseUrl = "https://sou.zhaopin.com/?kw=%s&jl=%s&p=";
    private List<String> blackList;
    private ChromeDriver driver;
    private WebDriverWait webDriverWait;

    public Zhaopin(String query, String city, List blackList) {
        baseUrl = String.format(baseUrl, query, city);
        this.blackList = blackList;
    }

    public void start() {
        log.info("开始智联招聘投递简历");
        driver = new ChromeDriver();
        webDriverWait = new WebDriverWait(driver, 10);
        login();
        for (int page = 1; page <= 10; page ++)
            if (!chat(baseUrl + page)) break;
        log.info("投递结束");
        driver.close();
    }

    @SneakyThrows
    private Boolean chat(String url) {
        driver.get(url);
        wait("joblist-box__item clearfix");

        List<String> list = new ArrayList<>();
        findBatch("joblist-box__item clearfix").forEach(o -> {
            // 检查黑名单
            if (!isInBlackList(find(o, "companyinfo__top").getText()))
                list.add(find(o, "jobinfo__name").getAttribute("href"));
        });

        String cur = driver.getWindowHandle();

        for (String i: list) {
            driver.get(i);
            wait("a-job-apply-button summary-plane__action");
            WebElement button = find("a-job-apply-button summary-plane__action");
            if (button.getText().contains("申请职位")) {
                button.click();
                Thread.sleep(1500);

                if (isElementExist("a-job-apply-error-message-panel__title")) {
                    log.info("今日投递次数已达上限，请明天再试");
                    return false;
                }

                String com = find("company__title").getText();
                String title = find("summary-plane__title").getText();
                log.info("已投递 {} | {}", com, title);
                Thread.sleep(1500);
                closeOther(cur);
            }
        }
        return true;
    }
    private void login() {
        driver.get(loginUrl);
        wait("zppp-panel-normal-bar__img");
        driver.findElementByClassName("zppp-panel-normal-bar__img").click();
        log.info("等待扫码登陆...");
        wait("job-recommend__title", 3600);
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
    private Boolean isElementExist(String className) {
        try {
            find(className);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    private void closeOther(String windowHandle) {
        Set<String> windowHandles = driver.getWindowHandles();
        for (String i: windowHandles)
            if (!i.equals(windowHandle)) {
                driver.switchTo().window(i);
                driver.close();
            }
        driver.switchTo().window(windowHandle);
    }

}
