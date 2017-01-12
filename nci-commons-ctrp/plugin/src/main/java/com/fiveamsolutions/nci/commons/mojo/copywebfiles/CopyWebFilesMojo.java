/**
 * The software subject to this notice and license includes both human readable
 * source code form and machine readable, binary, object code form. The nci-commons
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
 * government employees are authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 *
 * This nci-commons Software License (the License) is between NCI and You. You (or
 * Your) shall mean a person or an entity, and all other entities that control,
 * are controlled by, or are under common control with the entity. Control for
 * purposes of this definition means (i) the direct or indirect power to cause
 * the direction or management of such entity, whether by contract or otherwise,
 * or (ii) ownership of fifty percent (50%) or more of the outstanding shares,
 * or (iii) beneficial ownership of such entity.
 *
 * This License is granted provided that You agree to the conditions described
 * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
 * no-charge, irrevocable, transferable and royalty-free right and license in
 * its rights in the nci-commons Software to (i) use, install, access, operate,
 * execute, copy, modify, translate, market, publicly display, publicly perform,
 * and prepare derivative works of the nci-commons Software; (ii) distribute and
 * have distributed to and by third parties the nci-commons Software and any
 * modifications and derivative works thereof; and (iii) sublicense the
 * foregoing rights set out in (i) and (ii) to third parties, including the
 * right to license such rights to further third parties. For sake of clarity,
 * and not by way of limitation, NCI shall have no right of accounting or right
 * of payment from You or Your sub-licensees for the rights granted under this
 * License. This License is granted at no charge to You.
 *
 * Your redistributions of the source code for the Software must retain the
 * above copyright notice, this list of conditions and the disclaimer and
 * limitation of liability of Article 6, below. Your redistributions in object
 * code form must reproduce the above copyright notice, this list of conditions
 * and the disclaimer of Article 6 in the documentation and/or other materials
 * provided with the distribution, if any.
 *
 * Your end-user documentation included with the redistribution, if any, must
 * include the following acknowledgment: This product includes software
 * developed by 5AM and the National Cancer Institute. If You do not include
 * such end-user documentation, You shall include this acknowledgment in the
 * Software itself, wherever such third-party acknowledgments normally appear.
 *
 * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
 * to endorse or promote products derived from this Software. This License does
 * not authorize You to use any trademarks, service marks, trade names, logos or
 * product names of either NCI or 5AM, except as required to comply with the
 * terms of this License.
 *
 * For sake of clarity, and not by way of limitation, You may incorporate this
 * Software into Your proprietary programs and into any third party proprietary
 * programs. However, if You incorporate the Software into third party
 * proprietary programs, You agree that You are solely responsible for obtaining
 * any permission from such third parties required to incorporate the Software
 * into such third party proprietary programs and for informing Your
 * sub-licensees, including without limitation Your end-users, of their
 * obligation to secure any required permissions from such third parties before
 * incorporating the Software into such third party proprietary software
 * programs. In the event that You fail to obtain such permissions, You agree
 * to indemnify NCI for any claims against NCI by such third parties, except to
 * the extent prohibited by law, resulting from Your failure to obtain such
 * permissions.
 *
 * For sake of clarity, and not by way of limitation, You may add Your own
 * copyright statement to Your modifications and to the derivative works, and
 * You may provide additional or different license terms and conditions in Your
 * sublicenses of modifications of the Software, or any derivative works of the
 * Software as a whole, provided Your use, reproduction, and distribution of the
 * Work otherwise complies with the conditions stated in this License.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO
 * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
 * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fiveamsolutions.nci.commons.mojo.copywebfiles;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author Scott Miller
 * @goal copy-web-files
 */
public class CopyWebFilesMojo extends AbstractMojo {
    private final String[] defaultFileTypes = {".jsp", ".jspf", ".css", ".js", ".jpg", ".gif", ".png", ".faces",
            ".tag", ".tagf"};

    /**
     * Files to copy.
     *
     * @parameter
     */
    private File[] srcDirectories;

    /**
     * Types of files to copy. Defaults to: jsp, jspf, css, js, jpg, gif, png, faces, tag, tagf
     *
     * @parameter
     */
    private String[] fileTypes;

    /**
     * The base directory where the files will be copies to.
     *
     * @parameter
     */
    private File deployDirectory;

    /**
     * The pattern of the sub directory in deployDirectory.
     *
     * @parameter
     */
    private String deployDirectoryPattern;

    /**
     * The name of the sub directory in the destination directory.
     *
     * @parameter
     */
    private String deploySubDirectory;

    /**
     * Whether to copy to all matching directories, or just to the most recently modified one.
     *
     * @parameter
     */
    @SuppressWarnings("PMD.ImmutableField")
    private boolean copyToAllMatches = false;

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (ArrayUtils.isEmpty(srcDirectories)) {
            getLog().info("No values given for srcDirectories, no files will be copied.");
            return;
        }

        if (deployDirectory == null || !deployDirectory.isDirectory()) {
            throw new MojoExecutionException("The deployDirectory configuration parameter must be set to a directory.");
        }

        if (ArrayUtils.isEmpty(fileTypes)) {
            fileTypes = defaultFileTypes;
        }

        deployDirectoryPattern = StringUtils.trimToNull(deployDirectoryPattern);

        Collection<File> latestDeployDirectories = getLatestDeploymentDirectories();

        for (File latestDeployDirectory : latestDeployDirectories) {
            IOFileFilter fileFilter = getFilefilter(latestDeployDirectory);
            copyFiles(latestDeployDirectory, fileFilter);
        }
    }

    /**
     * @param latestDeployDirectory
     * @param fileFilter
     * @throws MojoExecutionException
     */
    @SuppressWarnings("rawtypes")
    private void copyFiles(File latestDeployDirectory, IOFileFilter fileFilter) throws MojoExecutionException {
        try {
            for (File srcDir : srcDirectories) {
                Collection filesToCopy = FileUtils.listFiles(srcDir, fileFilter, TrueFileFilter.INSTANCE);
                int count = 0;
                for (Object o : filesToCopy) {
                    File src = (File) o;
                    String relativePath = StringUtils.removeStart(src.getAbsolutePath(), srcDir.getAbsolutePath());
                    File dest = new File(latestDeployDirectory.getAbsoluteFile() + relativePath);
                    if (src.lastModified() > dest.lastModified()) {
                        getLog().debug("Copying " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());
                        FileUtils.copyFile(src, dest);
                        count++;
                    }
                }
                getLog().info("Copied " + count + " files from "  + srcDir.getAbsolutePath()
                        + " to " + latestDeployDirectory.getAbsolutePath());
                FileUtils.copyDirectory(srcDir, latestDeployDirectory, fileFilter);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy web files", e);
        }
    }

    /**
     * @param latestDeployDirectory
     * @return
     */
    private IOFileFilter getFilefilter(File latestDeployDirectory) {
        IOFileFilter fileFilter = new SuffixFileFilter(fileTypes);
        fileFilter = FileFilterUtils.andFileFilter(FileFileFilter.FILE, fileFilter);
        fileFilter = FileFilterUtils.andFileFilter(new AgeFileFilter(latestDeployDirectory, false), fileFilter);
        return fileFilter;
    }

    /**
     * @return
     * @throws MojoExecutionException
     */
    private Collection<File> getLatestDeploymentDirectories() throws MojoExecutionException {
        Set<File> matchSet = new HashSet<File>();
        File latestDeployDirectory = deployDirectory;
        if (StringUtils.isNotEmpty(deployDirectoryPattern)) {
            IOFileFilter fileFilter =  new WildcardFileFilter(deployDirectoryPattern);
            fileFilter = FileFilterUtils.andFileFilter(DirectoryFileFilter.DIRECTORY, fileFilter);
            File[] matches = deployDirectory.listFiles((FileFilter) fileFilter);
            if (ArrayUtils.isEmpty(matches)) {
                throw new MojoExecutionException("No directories found that match the given pattern");
            }
            latestDeployDirectory = matches[0];
            for (File match : matches) {
                if (match.lastModified() > latestDeployDirectory.lastModified()) {
                    latestDeployDirectory = match;
                }
                if (copyToAllMatches) {
                    matchSet.add(match);
                }
            }
        }
        matchSet.add(latestDeployDirectory);
        return appendSubDirectory(matchSet);
    }

    private Collection<File> appendSubDirectory(Collection<File> matchSet) {
        Set<File> latestDeployDirectories = new HashSet<File>();
        for (File match : matchSet) {
            File processedMatch = match;
            if (StringUtils.isNotEmpty(deploySubDirectory)) {
                processedMatch = new File(match.getAbsolutePath() + File.separator + deploySubDirectory);
            }
            latestDeployDirectories.add(processedMatch);
        }
        return latestDeployDirectories;
    }
}
