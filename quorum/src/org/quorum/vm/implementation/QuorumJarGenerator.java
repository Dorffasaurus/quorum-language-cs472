/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quorum.vm.implementation;

import org.quorum.vm.interfaces.Dependency;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A program to convert the build directory into an executable jar file.
 * 
 * @author Andreas Stefik
 */
public class QuorumJarGenerator {
    
    private Manifest manifest;
    private File writeLocation;
    private JarOutputStream jar;
    private String main;
    private final String CREATED_BY = "Quorum 1.0";
    public ArrayList<File> files;
    public File buildDirectory;
    private Collection<Dependency> dependencies;
    private final String DEPENDENCIES_FOLDER = "libraries";
    
    public QuorumJarGenerator() {
        files = new ArrayList<File>();
    }
    
    public void writeJarFile(File directory) {
        buildDirectory = directory;
        FileOutputStream stream = null;
        JarOutputStream target = null;
        try {
            createNewManifest();
            stream = new FileOutputStream(writeLocation);
            target = new JarOutputStream(stream, manifest);
            add(directory, target);
        } catch (Exception ex) {
            Logger.getLogger(QuorumJarGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                target.close();
            } catch (IOException ex) {
                Logger.getLogger(QuorumJarGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(QuorumJarGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        //now write dependencies to disk
        File dependencyFolder = new File(buildDirectory + "/" + DEPENDENCIES_FOLDER);
        if(!dependencyFolder.isDirectory()) {
            dependencyFolder.mkdir();
        }
        
        File writeFolder = new File(writeLocation.getParent() + "/" + DEPENDENCIES_FOLDER);
        if (!writeFolder.exists()) {
            writeFolder.mkdirs();
        }
        try {
            Iterator<Dependency> it = dependencies.iterator();
            while(it.hasNext()) {
                Dependency next = it.next();
                File file = next.getFile();
                
                String relativePath = next.getRelativePath();
                File dep = new File(writeFolder  + "/" + relativePath + file.getName());
                copyFile(file, dep);
            }
        } catch (IOException ex) {
            Logger.getLogger(QuorumJarGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void copyFile(File file, File to) throws IOException {
        if(file.isFile()) { //copy it to the libraries folder
            if (!to.exists()) {
                to.getParentFile().mkdirs();
                to.createNewFile();
            }
            else {
                to.delete();
                to.createNewFile();
            }
            InputStream in = new FileInputStream(file);
            OutputStream out = new FileOutputStream(to);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        else { //copy the whole folder
            if (!to.exists()) {
                to.mkdirs();
            }
            
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                copyFile(new File(file, children[i]),
                        new File(to, children[i]));
            }
        }
    }
    
    private void add(File source, JarOutputStream target) throws IOException {
        //taken in part from http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        BufferedInputStream in = null;
        try {
            if (buildDirectory.getAbsolutePath().compareTo(source.getAbsolutePath()) == 0) {
                for (File nestedFile : source.listFiles()) {
                    add(nestedFile, target);
                }
            } else {
                int pathLength = buildDirectory.getAbsolutePath().length();
                String absolute = source.getAbsolutePath();

                String relative = absolute.substring(pathLength + 1);
                String name = relative.replace("\\", "/");

                if (source.isDirectory()) {
                    if (!name.isEmpty()) {
                        if (!name.endsWith("/")) {
                            name += "/";
                        }
                        JarEntry entry = new JarEntry(name);
                        entry.setTime(source.lastModified());
                        target.putNextEntry(entry);
                        target.closeEntry();
                    }
                    for (File nestedFile : source.listFiles()) {
                        add(nestedFile, target);
                    }
                    return;
                }

                JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                in = new BufferedInputStream(new FileInputStream(source));

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void createNewManifest() {
        final String ENCODING = "UTF-8";
        String version = Attributes.Name.MANIFEST_VERSION + ": 1.0\n";
        String created = "Created-By: " + CREATED_BY + "\n";
        String mainClass = Attributes.Name.MAIN_CLASS + ": " + getMain() + "\n";
        
        String libs = "";
        if(dependencies != null) {
            Iterator<Dependency> iterator = dependencies.iterator();
            while(iterator.hasNext()) {
                Dependency next = iterator.next();
                File file = next.getFile();
                if(file.isFile() && next.isExecutionDependency()) {
                    libs += Attributes.Name.CLASS_PATH + ": " + DEPENDENCIES_FOLDER + 
                            "/" + file.getName();
                }
            }
        }
        
        libs += "\n";
        String total = version + created + libs + mainClass + "\n";
        try {
            InputStream stream = new ByteArrayInputStream(total.getBytes(ENCODING));
            manifest = new Manifest();
            manifest.read(stream);
        } catch (IOException ex) {
            Logger.getLogger(QuorumJarGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Passes a collection of file dependencies. If an individual element is
     * a file, a dependency is created. If it is a folder, it is ignored.
     * 
     * @param libs 
     */
    public void setDependencies(Collection<Dependency> libs) {
        dependencies = libs;
    }
    
    /**
     * Returns where on disk a jar file will be written.
     * 
     * @return the writeLocation
     */
    public File getWriteLocation() {
        return writeLocation;
    }

    /**
     * Sets where on disk the jar file will be written.
     * 
     * @param writeLocation the writeLocation to set
     */
    public void setWriteLocation(File writeLocation) {
        this.writeLocation = writeLocation;
    }

    /**
     * @return the main
     */
    public String getMain() {
        return main;
    }

    /**
     * @param main the main to set
     */
    public void setMain(String main) {
        this.main = main;
    }
}
