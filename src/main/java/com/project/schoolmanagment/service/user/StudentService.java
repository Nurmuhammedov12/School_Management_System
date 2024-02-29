package com.project.schoolmanagment.service.user;

import com.project.schoolmanagment.entity.concretes.buisnes.LessonProgram;
import com.project.schoolmanagment.entity.concretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.payload.mappers.UserMapper;
import com.project.schoolmanagment.payload.messages.SuccesMessages;
import com.project.schoolmanagment.payload.request.buisnes.ChooseLessonProgramRequest;
import com.project.schoolmanagment.payload.request.user.StudentRequest;
import com.project.schoolmanagment.payload.response.businnes.ResponseMessage;
import com.project.schoolmanagment.payload.response.user.StudentResponse;
import com.project.schoolmanagment.repository.user.UserRepository;
import com.project.schoolmanagment.service.buisnes.LessonProgramService;
import com.project.schoolmanagment.service.helper.MetodHelper;
import com.project.schoolmanagment.service.validator.DateTimeValidator;
import com.project.schoolmanagment.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final PasswordEncoder passwordEncoder;
    private final LessonProgramService lessonProgramService;
    private final DateTimeValidator dateTimeValidator;
    private final MetodHelper methodHelper;
    private final UserMapper userMapper;

    public ResponseMessage<StudentResponse> saveStudent(StudentRequest studentRequest) {
        //do we really have a user with this ID
        User advisorTeacher = methodHelper.idUserExist(studentRequest.getAdvisorTeacherId());
        // check if this user advisor teacher
        methodHelper.isAdvisor(advisorTeacher);

        uniquePropertyValidator.checkDuplicate(
                studentRequest.getUsername(),
                studentRequest.getSsn(),
                studentRequest.getPhoneNumber(),
                studentRequest.getEmail());

        User student = userMapper.mapUser(studentRequest);
        //set missing properties
        student.setAdvisorTeacherId(advisorTeacher.getId());
        student.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
        student.setActive(true);
        student.setIsAdvisor(false);
        student.setStudentNumber(getLastNumber());

        return ResponseMessage.<StudentResponse>builder()
                .object(userMapper.mapUserToStudentResponse(userRepository.save(student)))
                .message(SuccesMessages.STUDENT_SAVE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private int getLastNumber(){
        if(!userRepository.findStudent(RoleType.STUDENT)){
            //first student
            return 1000;
        }
        return userRepository.getMaxStudentNumber() + 1;
    }




    public ResponseMessage<StudentResponse> addLessonProgram(HttpServletRequest httpServletRequest,
                                                             ChooseLessonProgramRequest lessonProgramRequest) {
        String username = (String) httpServletRequest.getAttribute("username");
        User loggedInStudent = methodHelper.loadUserByName(username);

        Set<LessonProgram> lessonProgramSet = lessonProgramService.getLessonProgramById(lessonProgramRequest.getLessonProgramId());

        Set<LessonProgram>existingLessonPrograms = loggedInStudent.getLessonProgramList();

        dateTimeValidator.checkLessonPrograms(existingLessonPrograms,lessonProgramSet);

        existingLessonPrograms.addAll(lessonProgramSet);
        loggedInStudent.setLessonProgramList(existingLessonPrograms);

        User savedStudent = userRepository.save(loggedInStudent);

        return ResponseMessage.<StudentResponse>builder()
                .message(SuccesMessages.LESSON_PROGRAM_ADD_TO_STUDENT)
                .object(userMapper.mapUserToStudentResponse(savedStudent))
                .httpStatus(HttpStatus.OK)
                .build();
    }
}