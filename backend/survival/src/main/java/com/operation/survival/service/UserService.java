package com.operation.survival.service;

import com.operation.survival.config.JwtUtils;
import com.operation.survival.dto.UserLoginDto;
import com.operation.survival.dto.UserRegisterDto;
import com.operation.survival.entity.Player;
import com.operation.survival.entity.User;
import com.operation.survival.entity.Weapon;
import com.operation.survival.repository.PlayerRepository;
import com.operation.survival.repository.UserRepository;
import com.operation.survival.repository.WeaponRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int BASE_HP = 140;
    private static final int STARTER_GOLD = 260;

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final WeaponRepository weaponRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(
            UserRepository userRepository,
            JwtUtils jwtUtils,
            PlayerRepository playerRepository,
            WeaponRepository weaponRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.playerRepository = playerRepository;
        this.weaponRepository = weaponRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(UserRegisterDto registerDto) {

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("帳號已被註冊！");
        }

        String encodedPassword =
                passwordEncoder.encode(registerDto.getPassword());

        User newUser = new User(
                registerDto.getUsername(),
                encodedPassword
        );

        User savedUser = userRepository.save(newUser);
        ensurePlayerAndWeapon(savedUser);

        return savedUser;
    }

    @Transactional
    public String login(UserLoginDto loginDto) {

        User user = userRepository
                .findByUsername(loginDto.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("帳號或密碼錯誤！"));

        if (!passwordEncoder.matches(
                loginDto.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("帳號或密碼錯誤！");
        }

        ensurePlayerAndWeapon(user);

        return jwtUtils.generateToken(user.getUsername());
    }

    @Transactional
    public void ensurePlayerAndWeapon(User user) {
        Player player = playerRepository.findByUser(user)
                .orElseGet(() -> {
                    Player newPlayer = new Player();
                    newPlayer.setNickname(user.getUsername());
                    newPlayer.setGold(STARTER_GOLD);
                    newPlayer.setCurrentStage(1);
                    newPlayer.setHp(BASE_HP);
                    newPlayer.setMaxHp(BASE_HP);
                    newPlayer.setLevel(1);
                    newPlayer.setExperience(0);
                    newPlayer.setPotionCount(2);
                    newPlayer.setTotalWins(0);
                    newPlayer.setSkillLevel(1);
                    newPlayer.setSkillCooldown(0);
                    newPlayer.setDifficulty("NORMAL");
                    newPlayer.setSaveVersion(4);
                    newPlayer.setUser(user);
                    return playerRepository.save(newPlayer);
                });

        boolean hasWeapon = weaponRepository.findByPlayer(player).stream()
                .anyMatch(weapon -> weapon.getPlayer().getId().equals(player.getId()));

        if (!hasWeapon) {
            Weapon weapon = new Weapon();
            weapon.setName("Pistol");
            weapon.setLevel(1);
            weapon.setAttack(16);
            weapon.setPlayer(player);
            weaponRepository.save(weapon);
        }
    }
}