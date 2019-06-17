import com.txz.bean.Mokey;
import com.txz.config.ImportConfig;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * description:
 * 测试ImportConfig类的配置
 *
 * @author Taxz
 * @create 2019-06-14 13:28
 */
public class TestCondition {
    AnnotationConfigApplicationContext application = new AnnotationConfigApplicationContext(ImportConfig.class);

    @Test
    public void testPrototype() {
        printBeans(application);
        Mokey mk1 = (Mokey) application.getBean("mk");
        Mokey mk2 = (Mokey) application.getBean("mk");
        System.out.println(mk1 == mk2);
        Mokey mkf = (Mokey) application.getBean("mokeyFactroy");
        System.out.println(mkf.getName());
    }


    private void printBeans(AnnotationConfigApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        System.out.println(String.join("\n",names));
    }

}
