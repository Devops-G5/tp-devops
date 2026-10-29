import com.mongodb.MongoException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler({
        DataAccessException.class,
        MongoException.class
    })
    public ResponseEntity<Map<String, String>> handleDatabaseException(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Database unavailable",
                    "message", "No se pudo completar la operación con la base de datos"
                ));
    }
}