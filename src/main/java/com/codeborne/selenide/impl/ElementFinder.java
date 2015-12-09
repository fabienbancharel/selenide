package com.codeborne.selenide.impl;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Proxy;
import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static java.lang.Thread.currentThread;

public class ElementFinder extends WebElementSource {
  public static SelenideElement wrap(By criteria) {
    return wrap(null, criteria, 0);
  }

  public static SelenideElement wrap(SearchContext parent, By criteria, int index) {
    return wrap(SelenideElement.class, parent, criteria, index);
  }

  @SuppressWarnings("unchecked")
  public static <T extends SelenideElement> T wrap(Class<T> clazz, SearchContext parent, By criteria, int index) {
    return (T) Proxy.newProxyInstance(
        currentThread().getContextClassLoader(),
        new Class<?>[]{clazz},
        new SelenideElementProxy(new ElementFinder(parent, criteria, index)));
  }

  private final SearchContext parent;
  private final By criteria;
  private final int index;

  ElementFinder(SearchContext parent, By criteria, int index) {
    this.parent = parent;
    this.criteria = criteria;
    this.index = index;
  }

  @Override
  public SelenideElement find(SelenideElement proxy, Object arg, int index) {
    return arg instanceof By ?
        wrap(proxy, (By) arg, index) :
        wrap(proxy, By.cssSelector((String) arg), index);
  }

  @Override
  public WebElement getWebElement() throws NoSuchElementException, IndexOutOfBoundsException {
    return index == 0 ?
        WebElementSelector.instance.findElement(getSearchContext(), criteria) :
        WebElementSelector.instance.findElements(getSearchContext(), criteria).get(index);
  }

  @Override
  public List<WebElement> findAll() throws NoSuchElementException, IndexOutOfBoundsException {
    return index == 0 ?
        WebElementSelector.instance.findElements(getSearchContext(), criteria) :
        super.findAll();
  }

  private SearchContext getSearchContext() {
    return parent == null ? getWebDriver() :
        (parent instanceof SelenideElement) ? ((SelenideElement) parent).toWebElement() :
        parent;
  }

  @Override
  public ElementNotFound createElementNotFoundError(Condition condition, Throwable lastError) {
    if (parent instanceof SelenideElement) {
      ((SelenideElement) parent).should(exist);
    }
    else if (parent instanceof WebElement) {
      $((WebElement) parent).should(exist);
    }
    
    return super.createElementNotFoundError(condition, lastError);
  }

  @Override
  public String getSearchCriteria() {
    return index == 0 ? 
        Describe.selector(criteria) : 
        Describe.selector(criteria) + '[' + index + ']';
  }

  @Override
  public String toString() {
    return "{" + getSearchCriteria() + '}';
  }
}
