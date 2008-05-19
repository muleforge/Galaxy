package org.mule.galaxy.web.client.util;

import java.util.EventListener;

public interface ValidationListener extends EventListener {

    /**
     *  This is where you can stuff like enable buttons, etc when
     *  an error gets reset. This method is optional 
     * @param source
     * @param error
     */
    public void onErrorReset(TextBoxBase source, String error);

    /**
     *  This is where you can stuff like enable buttons, etc when
     *  an error occurs. This method is optional 
     * @param source
     * @param error
     */
    public void onError(TextBoxBase source, String error);


    /**
     * optional method to do some custom validation 
     * @param source
     */
    public void onValidation(TextBoxBase source);

}
