package org.magnum.mobilecloud.video;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.security.Principal;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Video service implementation.
 */
@Controller
public class VideoSvcImpl {
	/**
	 * Video service URL path (root).
	 */
	private static final String VIDEO_SVC_PATH = "/video";
	@Autowired
	VideoRepository videoRepo;

	@RequestMapping(value = VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public @ResponseBody
	void likeVideo(@PathVariable(value = "id") long id,
			HttpServletResponse response, Principal user) {
		final Video v = videoRepo.findOne(id);
		String activeUserName = user.getName();
		if (v == null) {
			response.setStatus(SC_NOT_FOUND);
		} else if (v.getUsersWithLikes().contains(activeUserName)) {
			response.setStatus(SC_BAD_REQUEST);
		} else {
			final HashSet<String> newLikers = Sets.newHashSet(v.getUsersWithLikes());
			newLikers.add(activeUserName);
			v.setUsersWithLikes(newLikers);
			v.setLikes(v.getLikes() + 1);
			videoRepo.save(v);
		}
	}

	// @RequestMapping(value = VIDEO_SVC_PATH + SEPARATOR + "{id}" + DATA,
	// method = RequestMethod.GET)
	// public void getData(@PathVariable(value = "id") long id,
	// HttpServletResponse response) {
	// try {
	// final Video v = loadById(id);
	// // return video
	// VideoFileManager fileManager = VideoFileManager.get();
	// if (fileManager.hasVideoData(v)) {
	// // copy video to the response's output
	// fileManager.copyVideoData(v, response.getOutputStream());
	// } else {
	// // no video found (not yet uploaded)
	// response.setStatus(SC_NOT_FOUND);
	// }
	// } catch (IllegalArgumentException e) {
	// response.setStatus(SC_NOT_FOUND);
	// } catch (IOException ioEx) {
	// response.setStatus(SC_INTERNAL_SERVER_ERROR);
	// }
	// }

	// /**
	// * Tries to load video for the given id.
	// *
	// * @param id
	// * the id of the requested video.
	// * @return the video which belongs to the given id.
	// * @throws IllegalArgumentException
	// * on invalid (not positive or non-existing) id.
	// */
	// public Video loadById(long id) throws IllegalArgumentException {
	// // not positive id
	// if (id < 1)
	// throw new IllegalArgumentException("Id must be positive");
	// // retrieve video
	// Video video = videos.get(id);
	// // no entry found for the specified id
	// if (video == null)
	// throw new IllegalArgumentException(
	// "No entry found for the specified id");
	// return video;
	// }

	/**
	 * @return local server URL base.
	 */
	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		String base = "http://"
				+ request.getServerName()
				+ ((request.getServerPort() != 80) ? ":"
						+ request.getServerPort() : "");
		return base;
	}
}
