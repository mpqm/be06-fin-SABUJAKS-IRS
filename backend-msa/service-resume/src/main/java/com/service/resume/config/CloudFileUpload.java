package com.service.resume.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.service.common.base.BaseException;
import com.service.common.base.BaseResponseMessage;
import com.service.common.dto.request.resume.CreateResumeReq;
import com.service.common.dto.request.resume.SubmitResumeReq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudFileUpload {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3;

    public String upload(MultipartFile file) throws BaseException {
        if(file != null){
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            try {
                String saveFileName = UUID.randomUUID()+ "_" + file.getOriginalFilename();
                amazonS3.putObject(bucketName, saveFileName, file.getInputStream(), metadata);
                return "https://"+bucketName+".s3.ap-northeast-2.amazonaws.com/"+saveFileName;
            }
            catch (IOException e) { throw new BaseException(BaseResponseMessage.FILE_UPLOAD_FAIL, e.getMessage()); }
        } else {
            return null;
        }
    }

    public List<String> multipleUpload(MultipartFile[] files) throws BaseException {
        if(files != null){
            List<String> fileNames = new ArrayList<>();
            for(MultipartFile file : files){
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());
                try {
                    String saveFileName = UUID.randomUUID()+ "_" + file.getOriginalFilename();
                    amazonS3.putObject(bucketName, saveFileName, file.getInputStream(), metadata);
                    fileNames.add("https://"+bucketName+".s3.ap-northeast-2.amazonaws.com/"+saveFileName);
                }
                catch (IOException e) { throw new BaseException(BaseResponseMessage.FILE_UPLOAD_FAIL, e.getMessage()); }
            }
            return fileNames;
        } else {
            return null;
        }
    }

    public String createResumeAttachmentsUpload(CreateResumeReq dto, MultipartFile[] portfolioFiles, MultipartFile file) throws BaseException{
        if(file != null){
            if(dto.getCodes().contains("resume_009")) {
                if (portfolioFiles != null && portfolioFiles.length > 0) {
                    List<String> portfolioUrls = multipleUpload(portfolioFiles);
                    int fileIndex = 0;
                    for (int i = 0; i < dto.getPortfolios().size(); i++) {
                        if (dto.getPortfolios().get(i).getPortfolioType().equals("파일")) {
                            if (fileIndex < portfolioFiles.length) { // TYPE이 파일이고 URL이 null인 경우
                                dto.getPortfolios().get(i).setPortfolioUrl(portfolioUrls.get(fileIndex));
                                fileIndex++;
                            }
                        }
                    }
                }
            }
            return upload(file);
        } else {
            return null;
        }
    }

    public String submitResumeAttachmentsUpload(SubmitResumeReq dto, MultipartFile[] portfolioFiles, MultipartFile file) throws BaseException{
        if(file != null){
            if(dto.getCodes().contains("resume_009")) {
                if (portfolioFiles != null && portfolioFiles.length > 0) {
                    List<String> portfolioUrls = multipleUpload(portfolioFiles);
                    int fileIndex = 0;
                    for (int i = 0; i < dto.getPortfolios().size(); i++) {
                        if (dto.getPortfolios().get(i).getPortfolioType().equals("파일")) {
                            if (fileIndex < portfolioFiles.length) { // TYPE이 파일이고 URL이 null인 경우
                                dto.getPortfolios().get(i).setPortfolioUrl(portfolioUrls.get(fileIndex));
                                fileIndex++;
                            }
                        }
                    }
                }
            }
            return upload(file);
        } else {
            return null;
        }
    }
}