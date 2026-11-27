
package com.jouletracker.platform.businessenergy.application.internal.commandservices;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.model.commands.DeleteBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.model.commands.UpdateBusinessAreaCommand;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessAreaCommandServiceImplTest {

    @Mock
    private BusinessAreaRepository businessAreaRepository;

    @InjectMocks
    private BusinessAreaCommandServiceImpl commandService;

    @Test
    void shouldCreateAreaWithNotificationRecipients() {

        var command = new CreateBusinessAreaCommand(
                1L,
                "Cocina",
                "1",
                "Área comercial",
                "restaurant",
                List.of(
                        "employee@example.com",
                        "manager@example.com"
                )
        );

        when(businessAreaRepository.save(any(BusinessArea.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = commandService.handle(command);

        assertTrue(result.isSuccess());

        var area = result.toOptional().orElseThrow();

        assertEquals(
                Set.of(
                        "employee@example.com",
                        "manager@example.com"
                ),
                area.getNotificationRecipients()
        );
    }

    @Test
    void shouldRejectInvalidNotificationRecipient() {

        var command = new CreateBusinessAreaCommand(
                1L,
                "Cocina",
                "1",
                "Área comercial",
                "restaurant",
                List.of("invalid-email")
        );

        var result = commandService.handle(command);

        assertTrue(result.isFailure());

        verify(businessAreaRepository, never())
                .save(any(BusinessArea.class));
    }

    @Test
    void shouldUpdateNotificationRecipients() {

        var area = createArea(1L);

        area.addNotificationRecipient("old@example.com");

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.of(area));

        when(businessAreaRepository.save(area))
                .thenReturn(area);

        var command = new UpdateBusinessAreaCommand(
                10L,
                1L,
                "Cocina",
                "1",
                "Área comercial",
                "restaurant",
                List.of("new@example.com")
        );

        var result = commandService.handle(command);

        assertTrue(result.isSuccess());

        assertEquals(
                Set.of("new@example.com"),
                area.getNotificationRecipients()
        );
    }

    @Test
    void shouldPreserveRecipientsWhenNotProvided() {

        var area = createArea(1L);

        area.addNotificationRecipient("employee@example.com");

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.of(area));

        when(businessAreaRepository.save(area))
                .thenReturn(area);

        var command = new UpdateBusinessAreaCommand(
                10L,
                1L,
                "Cocina",
                "1",
                "Nueva descripción",
                "restaurant",
                null
        );

        var result = commandService.handle(command);

        assertTrue(result.isSuccess());

        assertEquals(
                Set.of("employee@example.com"),
                area.getNotificationRecipients()
        );
    }

    @Test
    void shouldClearRecipientsWhenEmptyListIsProvided() {

        var area = createArea(1L);

        area.addNotificationRecipient("employee@example.com");

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.of(area));

        when(businessAreaRepository.save(area))
                .thenReturn(area);

        var command = new UpdateBusinessAreaCommand(
                10L,
                1L,
                "Cocina",
                "1",
                "Área comercial",
                "restaurant",
                List.of()
        );

        assertTrue(
                commandService.handle(command).isSuccess()
        );

        assertTrue(area.getNotificationRecipients().isEmpty());
    }

    @Test
    void shouldRejectUpdateOfAnotherUsersArea() {

        var area = createArea(2L);

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.of(area));

        var command = new UpdateBusinessAreaCommand(
                10L,
                1L,
                "Cocina",
                "1",
                "Área comercial",
                "restaurant",
                List.of("employee@example.com")
        );

        assertTrue(
                commandService.handle(command).isFailure()
        );

        verify(businessAreaRepository, never())
                .save(any(BusinessArea.class));
    }

    @Test
    void shouldDeleteOwnBusinessArea() {

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.of(createArea(1L)));

        var result = commandService.handle(
                new DeleteBusinessAreaCommand(10L, 1L)
        );

        assertTrue(result.isSuccess());

        verify(businessAreaRepository).deleteById(10L);
    }

    @Test
    void shouldRejectDeletionOfAnotherUsersArea() {

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.of(createArea(2L)));

        var result = commandService.handle(
                new DeleteBusinessAreaCommand(10L, 1L)
        );

        assertTrue(result.isFailure());

        verify(businessAreaRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void shouldRejectDeletionOfNonexistentArea() {

        when(businessAreaRepository.findById(10L))
                .thenReturn(Optional.empty());

        var result = commandService.handle(
                new DeleteBusinessAreaCommand(10L, 1L)
        );

        assertTrue(result.isFailure());

        verify(businessAreaRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void shouldRejectDeletionWithoutUserId() {

        var result = commandService.handle(
                new DeleteBusinessAreaCommand(10L, null)
        );

        assertTrue(result.isFailure());

        verifyNoInteractions(businessAreaRepository);
    }

    private BusinessArea createArea(Long userId) {

        return new BusinessArea(
                10L,
                userId,
                "Cocina",
                "1",
                "Área comercial",
                "restaurant"
        );
    }
}
