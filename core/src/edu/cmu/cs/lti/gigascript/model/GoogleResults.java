package edu.cmu.cs.lti.gigascript.model;

import java.util.List;

public class GoogleResults {

        private ResponseData responseData;

        public ResponseData getResponseData() {
            return responseData;
        }

        public void setResponseData(ResponseData responseData) {
            this.responseData = responseData;
        }

        public String toString() {
            return "ResponseData[" + responseData + "]";
        }

        public static class ResponseData {
            private List<Result> results;

            public List<Result> getResults() {
                return results;
            }

            public void setResults(List<Result> results) {
                this.results = results;
            }

            public String toString() {
                return "Results[" + results + "]";
            }
        }

        public static class Result {
            private String url;
            private String title;
            private String content;

            public String getUrl() {
                return url;
            }

            public String getTitle() {
                return title;
            }

            public String getContent() {
                return content.replaceAll("<b>...</b>$","").replaceAll("^<b>...</b>","").replace("<b>", "").replace("</b>", "").replace("&#39;", "'");
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String toString() {
                return "Result[url:" + url + ",title:" + title + "]";
            }


            public void setContent(String content) {
                this.content = content;
            }
        }
    }