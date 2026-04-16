package controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.inject.Named;

@Named("programmaticBean")
@RequestScoped
public class programmaticBean {
    private HtmlPanelGroup container;
    public programmaticBean() {
           container = new HtmlPanelGroup();
           container.setLayout("block");
           container.setStyleClass("text-center p-4");
           HtmlOutputText name = new HtmlOutputText();
           name.setValue("สมุนไพรสูตรลับ- ชาเขียวออร์แกนิก");
           name.setStyleClass("block text-2xl font-bold text-primary mb-3");

           HtmlOutputText price = new HtmlOutputText();
           price.setValue("ราคาพิเศษ: $19.99");
           price.setStyleClass("block text-xl mb-4");
           
           container.getChildren().add(name);
           container.getChildren().add(price);
       }
    public HtmlPanelGroup getContainer() {
        return container;
    }

    public void setContainer(HtmlPanelGroup container) {
        this.container = container;
    }
     
}
