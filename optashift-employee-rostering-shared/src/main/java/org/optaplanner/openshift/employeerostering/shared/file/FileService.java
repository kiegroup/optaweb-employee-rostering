package org.optaplanner.openshift.employeerostering.shared.file;


public interface FileService {

    /**
     * Writes a text file, overwriting its contents if it exists
     * 
     * @param tenantId owner of the file
     * @param fileName name of the file
     * @param data contents of the file
     */
    void writeFile(Integer tenantId, String fileName, String data);

    /**
     * Deletes a text file
     * 
     * @param tenantId owner of the file
     * @param fileName name of the file
     */
    void deleteFile(Integer tenantId, String fileName);

    /**
     * Returns the content of a text file
     * 
     * @param tenantId owner of the file
     * @param fileName name of the file
     * @return Contents of the file fileName for tenant tenantId
     */
    String getFileData(Integer tenantId, String fileName);

    /**
     * Check if a file exists
     * 
     * @param tenantId owner of the file
     * @param fileName name of the file
     * @return true iff the tenant tenantId has a file with name fileName
     */
    boolean fileExists(Integer tenantId, String fileName);
}
