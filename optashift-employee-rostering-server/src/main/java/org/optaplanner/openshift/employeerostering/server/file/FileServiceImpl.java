package org.optaplanner.openshift.employeerostering.server.file;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.shared.file.FileService;
import org.optaplanner.openshift.employeerostering.shared.file.TextFile;

public class FileServiceImpl extends AbstractRestServiceImpl implements FileService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void writeFile(Integer tenantId, String fileName, String data) {
        List<TextFile> blocks = entityManager.createNamedQuery("TextFile.fetchFile", TextFile.class)
                .setParameter("tenantId", tenantId)
                .setParameter("fileName", fileName)
                .getResultList();
        
        final int BLOCKS_NEEDED_TO_HOLD_DATA = (int)Math.round(Math.ceil((data.length() + 0.0)/TextFile.BLOCK_SIZE));
        int remainder = data.length();
        for (int i = 0; i < Math.min(BLOCKS_NEEDED_TO_HOLD_DATA, blocks.size()); i++) {
            TextFile block = blocks.get(i);
            String blockContents = data.substring(i * TextFile.BLOCK_SIZE, Math.min(i * TextFile.BLOCK_SIZE + remainder,
                    (i + 1) * TextFile.BLOCK_SIZE));
            block.setData(blockContents);
            entityManager.merge(block);
        }
        
        if (BLOCKS_NEEDED_TO_HOLD_DATA > blocks.size()) {
            for (int i = blocks.size(); i < BLOCKS_NEEDED_TO_HOLD_DATA; i++) {
                String blockContents = data.substring(i * TextFile.BLOCK_SIZE, Math.min(i * TextFile.BLOCK_SIZE
                        + remainder,
                        (i + 1) * TextFile.BLOCK_SIZE));
                TextFile block = new TextFile(tenantId, fileName, i, blockContents);
                entityManager.persist(block);
            }
        }
        else if (BLOCKS_NEEDED_TO_HOLD_DATA < blocks.size()) {
            for (int i = BLOCKS_NEEDED_TO_HOLD_DATA; i < blocks.size(); i++) {
                TextFile block = blocks.get(i);
                entityManager.remove(block);
            }
        }
    }

    @Override
    @Transactional
    public void deleteFile(Integer tenantId, String fileName) {
        List<TextFile> blocks = entityManager.createNamedQuery("TextFile.fetchFile", TextFile.class)
                .setParameter("tenantId", tenantId)
                .setParameter("fileName", fileName)
                .getResultList();

        if (blocks.isEmpty()) {
            throw new IllegalStateException("File " + fileName + " does not exist");
        }

        for (TextFile file : blocks) {
            entityManager.remove(file);
        }
    }

    @Override
    public String getFileData(Integer tenantId, String fileName) {
        List<TextFile> blocks = entityManager
                .createNamedQuery("TextFile.fetchFile", TextFile.class)
                .setParameter("tenantId", tenantId)
                .setParameter("fileName", fileName)
                .getResultList();

        if (blocks.isEmpty()) {
            throw new IllegalStateException("File " + fileName + " does not exist");
        }

        StringBuilder out = new StringBuilder();
        blocks.forEach((b) -> out.append(b.getData()));
        return out.toString();
    }

    @Override
    public boolean fileExists(Integer tenantId, String fileName) {
        return entityManager
                .createNamedQuery("TextFile.fetchFile", TextFile.class)
                .setParameter("tenantId", tenantId)
                .setParameter("fileName", fileName)
                .getResultList()
                .isEmpty();
    }

}
