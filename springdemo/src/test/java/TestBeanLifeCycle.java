import com.txz.config.BeanLifeCycle;
import com.txz.config.BeanScansConfig;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * description:
 *
 * @author Taxz
 * @create 2019-06-14 16:26
 */
public class TestBeanLifeCycle {
    AnnotationConfigApplicationContext context  = new AnnotationConfigApplicationContext(BeanLifeCycle.class);

    @Test
    public void testBeanLifeCycle() {
        printBeans(context);
        context.close();
    }

    private void printBeans(AnnotationConfigApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        System.out.println(String.join("\n",names));
    }
}
