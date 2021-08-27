package esproject.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Secret OSGI configuration file for project. SecretConfiguration class only for OSGI config values.
 *
 * @author yograna
 *
 */
@ObjectClassDefinition(name = "Secret Configuration", description = "Secret OSGI Configuration")
public @interface SecretConfiguration {

    
    @AttributeDefinition(
            name = "Elastic Search User Name",
            description = "Elastic Search User Name"
                    + "For example: esuser")
    String getEsUser() default "esuser";
    
    
    @AttributeDefinition(
            name = "Elastic Search Password",
            description = "Elastic Search Password"
                    + "For example: es,@37dftdf")
    String getEsPassword() default "es,@37dftdf";
    
}

