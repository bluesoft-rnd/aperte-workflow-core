
package org.aperteworkflow.samples.application.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegisterApplicationRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterApplicationRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="surname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attachmentBase64" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attachmentName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attachmentMimeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterApplicationRequestType", propOrder = {
    "name",
    "surname",
    "description",
    "attachmentBase64",
    "attachmentName",
    "attachmentMimeType"
})
public class RegisterApplicationRequestType {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String surname;
    @XmlElement(required = true)
    protected String description;
    @XmlElement(required = true)
    protected String attachmentBase64;
    @XmlElement(required = true)
    protected String attachmentName;
    @XmlElement(required = true)
    protected String attachmentMimeType;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the surname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the value of the surname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSurname(String value) {
        this.surname = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the attachmentBase64 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentBase64() {
        return attachmentBase64;
    }

    /**
     * Sets the value of the attachmentBase64 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentBase64(String value) {
        this.attachmentBase64 = value;
    }

    /**
     * Gets the value of the attachmentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentName() {
        return attachmentName;
    }

    /**
     * Sets the value of the attachmentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentName(String value) {
        this.attachmentName = value;
    }

    /**
     * Gets the value of the attachmentMimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentMimeType() {
        return attachmentMimeType;
    }

    /**
     * Sets the value of the attachmentMimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentMimeType(String value) {
        this.attachmentMimeType = value;
    }

}
