package universalgroup.UTaxi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import universalgroup.UTaxi.Entity.template.Attachment;

import java.util.UUID;

@CrossOrigin
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    Attachment findAttachmentById(UUID id);
}
